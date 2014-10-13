package gsonDB.index.fts;

import com.google.common.base.Preconditions;
import gsonDB.DB;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    public static InvertedIndex getInstance(final DB db, Class<?> entityType) throws IOException {
        File indexFile = new File(db.getDBDir().getPath() + File.pathSeparator + "__fts_" + entityType.getSimpleName());
        return new InvertedIndex(db, JsonInvertedIndexStore.getInstance(indexFile));
    }


    public void index(long documentId, String tokens) {
        Preconditions.checkNotNull(documentId);
        Preconditions.checkNotNull(tokens);
        Preconditions.checkArgument(tokens.trim().length() > 0, "tokens should not be an empty string or white spaces");

        StringTokenizer stringTokenizer = new StringTokenizer(tokens," ");
        //stringTokenizer.
    }

    public Set<Long> search(String tokens) {
        throw null;
    }

    public Map<String, List<IndexTuple>> getIndexMap() {
        return Collections.unmodifiableMap(this.indexMap);
    }
}
