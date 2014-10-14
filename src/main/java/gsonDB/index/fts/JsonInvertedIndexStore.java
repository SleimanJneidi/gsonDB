package gsonDB.index.fts;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Sleiman on 12/10/2014.
 */
public class JsonInvertedIndexStore implements InvertedIndexStore {

    private final File indexFile;

    public static JsonInvertedIndexStore getInstance(File indexFile) {
        return new JsonInvertedIndexStore(indexFile);
    }

    private JsonInvertedIndexStore(File indexFile) {
        this.indexFile = indexFile;
    }

    @Override
    public Map<String, List<IndexTuple>> load() throws IOException {
        if (!indexFile.exists()) {
            return new ConcurrentHashMap<>();
        }
        final byte[] jsonByteBuffer = Files.readAllBytes(indexFile.toPath());
        String jsonString = new String(jsonByteBuffer);
        Gson gson = new Gson();
        ConcurrentHashMap<String, List<IndexTuple>> indexMap = gson.fromJson(jsonString, new ConcurrentHashMap<String, List<IndexTuple>>().getClass());
        return indexMap;
    }

    @Override
    public void store(Map<String, List<IndexTuple>> invertedIndexMap) throws IOException {
        Preconditions.checkNotNull(invertedIndexMap);
        Gson gson = new Gson();
        String jsonString = gson.toJson(invertedIndexMap);
        Files.write(indexFile.toPath(), jsonString.getBytes());
    }
}
