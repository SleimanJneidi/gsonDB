package gsonDB.index.fts;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.sun.tools.javac.util.Pair;
import gsonDB.DB;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple inverted index implementation @see<a href="http://en.wikipedia.org/wiki/Inverted_index"/>
 * it uses a map of string and index tuple
 * which could be enhanced using a Radix Trie which helps in finding sub-strings,
 * I don't know about persisting the trie though (space wise)
 * <p/>
 * Created by Sleiman on 12/10/2014.
 */

public class InvertedIndex implements TextIndex {

    private final InvertedIndexStore indexStore;

    private InvertedIndex(InvertedIndexStore indexStore) throws IOException {
        this.indexStore = indexStore;
    }

    public static InvertedIndex getInstance(Class<?> entityType, DB db) throws IOException {
        File indexFile = new File(db.getDBDir().getPath() + File.separator + entityType.getSimpleName() + "__fts");
        return new InvertedIndex(JsonInvertedIndexStore.getInstance(indexFile));
    }


    public void index(long documentId, String tokens) {
        Preconditions.checkNotNull(documentId);
        Preconditions.checkNotNull(tokens);
        Preconditions.checkArgument(tokens.trim().length() > 0, "tokens should not be an empty string or white spaces");

        final List<Pair<String, Integer>> tokenWithPositionList = this.tokenize(tokens);

        Map<String, List<IndexTuple>> indexMap = indexStore.loadAllEntries();

        for (Pair<String, Integer> stringPositionPair : tokenWithPositionList) {
            final IndexTuple indexTuple = new IndexTuple(documentId, stringPositionPair.snd);
            if (indexMap.containsKey(stringPositionPair.fst)) {
                List<IndexTuple> existingIndexTuples = indexMap.get(stringPositionPair.fst);
                existingIndexTuples.add(indexTuple);
            } else {
                List<IndexTuple> newIndexTuples = new ArrayList<>();
                newIndexTuples.add(indexTuple);
                indexMap.put(stringPositionPair.fst, newIndexTuples);
            }
        }
        indexStore.store(indexMap);

    }

    public Set<Long> search(String stringToSearch) {
        Preconditions.checkNotNull(stringToSearch);
        Preconditions.checkArgument(stringToSearch.trim().length() > 0);

        Set<Long> results = new HashSet<>();
        Map<String, List<IndexTuple>> indexMap = indexStore.loadEntriesWithTokens(stringToSearch);

        Collection<List<Long>> flattenedDocumentIds = Maps.transformValues(indexMap, new Function<List<IndexTuple>, List<Long>>() {
            @Override
            public List<Long> apply(List<IndexTuple> input) {
                return new ArrayList<>(Collections2.transform(input, new Function<IndexTuple, Long>() {
                    @Override
                    public Long apply(IndexTuple input) {
                        return input.getDocumentId();
                    }
                }));
            }
        }).values();


        return results;
    }

    private List<Pair<String, Integer>> tokenize(String tokens) {
        List<Pair<String, Integer>> tokenWithPositionList = new ArrayList<>();
        final char[] charArray = tokens.toCharArray();
        int i = 0;
        while (i < charArray.length) {
            if (Character.isWhitespace(charArray[i])) {
                i++;
                continue;
            } else {
                int position = i;
                StringBuilder builder = new StringBuilder();
                while (i < charArray.length && !Character.isWhitespace(charArray[i])) {
                    builder.append(charArray[i]);
                    i++;
                }
                tokenWithPositionList.add(new Pair<>(builder.toString(), position));
            }
        }
        return tokenWithPositionList;
    }

    /**
     * Created by Sleiman on 12/10/2014.
     */
    private static class JsonInvertedIndexStore implements InvertedIndexStore {

        private final File indexFile;
        private Lock lock = new ReentrantLock();

        public static JsonInvertedIndexStore getInstance(File indexFile) {
            return new JsonInvertedIndexStore(indexFile);
        }

        private JsonInvertedIndexStore(File indexFile) {
            this.indexFile = indexFile;
        }

        @Override
        public Map<String, List<IndexTuple>> loadEntriesWithTokens(String stringOfTokens) {
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
                    if (Arrays.binarySearch(split, token) >= 0) {
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
        public Map<String, List<IndexTuple>> loadAllEntries() {
            if (!indexFile.exists()) {
                return new ConcurrentHashMap<>();
            }
            byte[] jsonByteBuffer;
            try {
                jsonByteBuffer = Files.readAllBytes(indexFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String jsonString = new String(jsonByteBuffer);
            Gson gson = new Gson();
            Type mapType = new TypeToken<ConcurrentHashMap<String, List<IndexTuple>>>() {
            }.getType();
            ConcurrentHashMap<String, List<IndexTuple>> indexMap = gson.fromJson(jsonString, mapType);
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
}
