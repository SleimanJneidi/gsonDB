package gsonDB.index.fts;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Sleiman on 12/10/2014.
 */
public class GsonInvertedIndexStore implements InvertedIndexStore {

    private final File indexFile;

    public static GsonInvertedIndexStore getInstance(File indexFile) {
        return new GsonInvertedIndexStore(indexFile);
    }

    private GsonInvertedIndexStore(File indexFile) {
        this.indexFile = indexFile;
    }

    @Override
    public Map<String, List<IndexTuple>> load() throws IOException {
        final byte[] jsonByteBuffer = Files.readAllBytes(indexFile.toPath());
        if (jsonByteBuffer.length == 0) {
            return new ConcurrentHashMap<>();
        }
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
