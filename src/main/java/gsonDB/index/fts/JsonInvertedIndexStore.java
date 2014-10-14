package gsonDB.index.fts;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sleiman on 12/10/2014.
 */
public class JsonInvertedIndexStore implements InvertedIndexStore {

    private final File indexFile;
    private Lock lock = new ReentrantLock();

    public static JsonInvertedIndexStore getInstance(File indexFile) {
        return new JsonInvertedIndexStore(indexFile);
    }

    private JsonInvertedIndexStore(File indexFile) {
        this.indexFile = indexFile;
    }

    @Override
    public Map<String, List<IndexTuple>> load(String stringOfTokens) {
        Preconditions.checkNotNull(stringOfTokens);
        Preconditions.checkArgument(stringOfTokens.trim().length() > 0);

        if (!indexFile.exists()) {
            return new ConcurrentHashMap<>();
        }
        final String[] split = stringOfTokens.split("\\s+");
        Arrays.sort(split);

        Map<String, List<IndexTuple>> tokenToIndexTupleMap = new ConcurrentHashMap<>();
        try (JsonReader reader = new JsonReader(new FileReader(indexFile))) {
            lock.lock();

            Gson gson = new Gson();
            reader.beginObject();

            while (reader.hasNext()) {
                String token = reader.nextName();
                if (Arrays.binarySearch(split,token) >= 0) {
                    List<IndexTuple> indexTuples = new ArrayList<>();

                    reader.beginArray();
                    while (reader.hasNext()) {
                        IndexTuple indexTuple = gson.fromJson(reader, IndexTuple.class);
                        indexTuples.add(indexTuple);
                    }
                    tokenToIndexTupleMap.put(token, indexTuples);
                    reader.endArray();

                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return tokenToIndexTupleMap;
    }

    @Override
    public Map<String, List<IndexTuple>> load() {
        if (!indexFile.exists()) {
            return new ConcurrentHashMap<>();
        }
        byte[] jsonByteBuffer = null;
        try {
            jsonByteBuffer = Files.readAllBytes(indexFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String jsonString = new String(jsonByteBuffer);
        Gson gson = new Gson();
        ConcurrentHashMap<String, List<IndexTuple>> indexMap = gson.fromJson(jsonString, new ConcurrentHashMap<String, List<IndexTuple>>().getClass());
        return indexMap;
    }

    @Override
    public void store(Map<String, List<IndexTuple>> invertedIndexMap) {
        Preconditions.checkNotNull(invertedIndexMap);
        Gson gson = new Gson();
        String jsonString = gson.toJson(invertedIndexMap);
        try {
            Files.write(indexFile.toPath(), jsonString.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
