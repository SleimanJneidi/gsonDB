package gsonDB.index.fts;

import com.google.common.base.Preconditions;
import com.sun.tools.javac.util.Pair;
import gsonDB.DB;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Sleiman on 12/10/2014.
 */
public class InvertedIndex implements TextIndex {

    private Map<String, List<IndexTuple>> indexMap;
    private final DB db;
    private final InvertedIndexStore indexStore;

    private InvertedIndex(final DB db, InvertedIndexStore indexStore) throws IOException {
        this.db = db;
        this.indexStore = indexStore;
        indexMap = indexStore.load();
    }

    public static InvertedIndex getInstance(Class<?> entityType, DB db) throws IOException {
        File indexFile = new File(db.getDBDir().getPath() + File.pathSeparator + "__fts_" + entityType.getSimpleName());
        return new InvertedIndex(db, JsonInvertedIndexStore.getInstance(indexFile));
    }


    public void index(long documentId, String tokens) {
        Preconditions.checkNotNull(documentId);
        Preconditions.checkNotNull(tokens);
        Preconditions.checkArgument(tokens.trim().length() > 0, "tokens should not be an empty string or white spaces");

        final List<Pair<String, Integer>> tokenWithPositionList = this.tokenize(tokens);

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

        try {
            this.indexStore.store(indexMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

    public Set<Long> search(String tokens) {
        throw null;
    }



    public Map<String, List<IndexTuple>> getIndexMap() {
        return Collections.unmodifiableMap(this.indexMap);
    }
}
