package gsonDB.index;

import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import gsonDB.DB;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Sleiman on 28/09/2014.
 */
class IndexHandler {

    private static final Map<Class<?>, IndexHandler> indexHandlers = new ConcurrentHashMap<>();

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
        final String indexFileName = entityType.getSimpleName()+ "_index";
        this.file = new File(db.getDbDir(),indexFileName);
        this.indexFile = new RandomAccessFile(this.file,"rw");
    }



    public int getNumberOfRecords() throws IOException {
        return fetchNumberOfRecords();
    }

    private int fetchNumberOfRecords() throws IOException {
        if(this.indexFile.length()==0){ // nothing to read
            return 0;
        }
        this.indexFile.seek(NUM_OF_RECORDS_FILE_POINTER);
        int numberOfRecords = this.indexFile.readInt();
        return numberOfRecords;
    }

    private Supplier<IndexKeyEntry> fetchNextIndexKeyEntry(){
        final Supplier<IndexKeyEntry> indexKeyEntrySupplier = new Supplier<IndexKeyEntry>() {
            @Override
            public IndexKeyEntry get() {
                IndexKeyEntry indexKeyEntry = null;
                byte[]keyBuffer = new byte[KEY_SIZE];
                try {
                    indexFile.readFully(keyBuffer);
                    String key = new String(keyBuffer);
                    long filePointer = indexFile.readLong();
                    int recordSize = indexFile.readInt();
                    indexKeyEntry = new IndexKeyEntry(key,filePointer,recordSize);
                    return indexKeyEntry;
                } catch (IOException e) {
                   e.printStackTrace(); //FIXME
                }
                return indexKeyEntry;
            }
        };
        return indexKeyEntrySupplier;
    }

    private Set<IndexKeyEntry> allIndexEntries() throws IOException {
        final int numberOfRecords = this.fetchNumberOfRecords();
        Set<IndexKeyEntry> indexKeyEntries = new HashSet<>(numberOfRecords);
        this.indexFile.seek(KEY_TABLE_FILE_POINTER);

        for(int i =0;i<numberOfRecords;i++){
          indexKeyEntries.add(this.fetchNextIndexKeyEntry().get());
        }
        return allIndexEntries();
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