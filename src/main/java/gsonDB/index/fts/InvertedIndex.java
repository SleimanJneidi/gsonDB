package gsonDB.index.fts;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
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

        Map<String, List<IndexTuple>> indexMap = indexStore.load();

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
        Map<String, List<IndexTuple>> indexMap = indexStore.load(stringToSearch);
        // TODO get documents intersection

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

}
