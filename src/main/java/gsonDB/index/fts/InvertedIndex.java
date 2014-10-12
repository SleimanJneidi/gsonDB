package gsonDB.index.fts;

import gsonDB.DB;
import sun.tools.java.Environment;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Sleiman on 12/10/2014.
 */
public class InvertedIndex {

    private final Map<String, List<IndexTuple>> indexMap = new ConcurrentHashMap<>();
    private final DB db;
    private final File indexFile;

    private InvertedIndex(final DB db, final File indexFile) {
        this.db = db;
        this.indexFile = indexFile;
    }

    public static InvertedIndex getInstance(final DB db, Class<?> entityType) {
        File indexFile = new File(db.getDBDir().getPath() + File.pathSeparator + "__fts_" + entityType.getSimpleName());
        return new InvertedIndex(db, indexFile);
    }

    public Map<String, List<IndexTuple>> getIndexMap() {
        return Collections.unmodifiableMap(this.indexMap);
    }
}
