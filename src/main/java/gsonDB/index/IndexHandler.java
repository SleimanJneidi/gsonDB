package gsonDB.index;

import gsonDB.DB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Sleiman on 28/09/2014.
 */
class IndexHandler {

    private static final Map<Class<?>, IndexHandler> indexHandlers = new ConcurrentHashMap<Class<?>, IndexHandler>();

    private final DB db;
    private final Class<?> entityType;
    private final File file;
    private final RandomAccessFile indexFile;

    private static final int NUM_OF_RECORDS_FILE_POINTER = 0; // file's head

    private static final int KEY_TABLE_FILE_POINTER = 4; // 4 bytes after the number of records
    private static final int KEY_SIZE = 32; // 32 bytes
    private static final int FILE_POINTER_SIZE = 8; // long value
    private static final int RECORD_LENGTH_SIZE = 4; // integer value
    private static final int INDEX_KEY_ENTRY_SIZE = KEY_SIZE + FILE_POINTER_SIZE + RECORD_LENGTH_SIZE;



    public static IndexHandler getIndexHandler(final Class<?> type,final DB db) throws FileNotFoundException {
        if (indexHandlers.containsKey(type)) {
            return indexHandlers.get(type);
        }
        IndexHandler newHandler = new IndexHandler(type,db);
        indexHandlers.put(type, newHandler);
        return newHandler;
    }

    private IndexHandler(final Class<?> entityType, final DB db) throws FileNotFoundException {
        this.db = db;
        this.entityType = entityType;
        final String indexFileName = entityType.getName()+"_index";
        this.file = new File(db.getDbDir(),indexFileName);
        this.indexFile = new RandomAccessFile(indexFileName,"rw");

    }

    private int fetchNumberOfRecords() throws IOException {
        this.indexFile.seek(NUM_OF_RECORDS_FILE_POINTER);
        int numberOfRecords = this.indexFile.readInt();
        return numberOfRecords;
    }

    public static Map<Class<?>, IndexHandler> allIndexHandlers(){
        return Collections.unmodifiableMap(indexHandlers);
    }

    public DB getDb() {
        return db;
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    protected File getFile() {
        return file;
    }

    protected RandomAccessFile getIndexFile() {
        return indexFile;
    }
}
