package gsonDB.index;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import gsonDB.DB;
import gsonDB.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sleiman on 03/10/2014.
 */
public class DefaultIndexProcessor extends IndexProcessor {

    private final Lock lock = new ReentrantLock();

    private static final int NUM_OF_RECORDS_FILE_POINTER = 0; // file's head

    private static final int KEY_TABLE_FILE_POINTER = 4; // 4 bytes after the number of records
    public static final int KEY_SIZE = 36; // 36 bytes
    protected static final int FILE_POINTER_SIZE = 8; // long value
    private static final int RECORD_LENGTH_SIZE = 4; // integer value
    private static final int INDEX_KEY_ENTRY_SIZE = KEY_SIZE + FILE_POINTER_SIZE + RECORD_LENGTH_SIZE;

    protected DefaultIndexProcessor(final Class<?> entityType, final DB db) throws FileNotFoundException {
        super(entityType, db);
    }

    @Override
    public int count() throws IOException {
        int numberOfRecords = fetchNumberOfRecords();
        return numberOfRecords == -1 ? 0 : numberOfRecords;
    }


    /**
     * @return number of records in the current index file, returns -1 of the file is empty
     * @throws java.io.IOException
     */
    private int fetchNumberOfRecords() throws IOException {
        if (this.indexFile.length() == 0) { // nothing to read
            return -1; // file is empty
        }
        this.indexFile.seek(NUM_OF_RECORDS_FILE_POINTER);
        int numberOfRecords = this.indexFile.readInt();
        return numberOfRecords;
    }

    private Supplier<IndexKeyEntry> fetchNextIndexKeyEntry() {
        final Supplier<IndexKeyEntry> indexKeyEntrySupplier = new Supplier<IndexKeyEntry>() {
            @Override
            public IndexKeyEntry get() {
                IndexKeyEntry indexKeyEntry = null;
                byte[] keyBuffer = new byte[KEY_SIZE];
                try {
                    indexFile.readFully(keyBuffer);
                    String key = new String(keyBuffer);
                    key = key.trim(); // the key is often less than 36 bytes
                    long filePointer = indexFile.readLong();
                    int recordSize = indexFile.readInt();
                    indexKeyEntry = new IndexKeyEntry(key, filePointer, recordSize);
                    return indexKeyEntry;
                } catch (IOException e) {
                    e.printStackTrace(); //FIXME
                }
                return indexKeyEntry;
            }
        };
        return indexKeyEntrySupplier;
    }

    protected List<IndexKeyEntry> allIndexEntries() throws IOException {
        final int numberOfRecords = this.count();
        List<IndexKeyEntry> indexKeyEntries = new ArrayList<>(numberOfRecords);
        this.indexFile.seek(KEY_TABLE_FILE_POINTER);

        for (int i = 0; i < numberOfRecords; i++) {
            indexKeyEntries.add(this.fetchNextIndexKeyEntry().get());
        }
        return indexKeyEntries;
    }

    public void insertNewIndexEntry(IndexKeyEntry indexKeyEntry) throws IOException {
        Preconditions.checkNotNull(indexKeyEntry);
        try {
            lock.lock();
            int numberOfRecords = count();
            numberOfRecords++;
            updateNumberOfRecords(numberOfRecords);

            this.indexFile.seek(this.indexFile.length());
            ByteBuffer keyByteBuffer = ByteBuffer.allocate(KEY_SIZE).put(indexKeyEntry.getKey().getBytes());
            byte[] keyBuffer = keyByteBuffer.array();

            this.indexFile.write(keyBuffer);
            this.indexFile.writeLong(indexKeyEntry.getDataFilePointer());
            this.indexFile.writeInt(indexKeyEntry.getRecordSize());
        } finally {
            lock.unlock();
        }
    }

    private void updateNumberOfRecords(int newNumberOfRecords) throws IOException {
        Preconditions.checkArgument(newNumberOfRecords >= 0);
        this.indexFile.seek(NUM_OF_RECORDS_FILE_POINTER);
        this.indexFile.writeInt(newNumberOfRecords);
    }

    public IndexKeyEntry getIndexByKey(String key) throws IOException {
        Preconditions.checkNotNull(key);
        int count = this.count();
        this.indexFile.seek(KEY_TABLE_FILE_POINTER);
        for (int i = 0; i < count; i++) {
            IndexKeyEntry keyEntry = fetchNextIndexKeyEntry().get();
            if (key.equals(keyEntry.getKey())) {
                return keyEntry;
            }
        }
        return null;
    }

    public void deleteIndexKeyEntry(IndexKeyEntry indexKeyEntry) throws IOException {
        try {
            this.lock.lock();

            indexFile.seek(KEY_TABLE_FILE_POINTER);
            long currentFilePointer;
            while ((currentFilePointer = indexFile.getFilePointer()) <= (indexFile.length() + INDEX_KEY_ENTRY_SIZE)) {
                IndexKeyEntry fetchedIndexedKeyEntry = fetchNextIndexKeyEntry().get();
                if (indexKeyEntry.equals(fetchedIndexedKeyEntry)) {
                    FileUtils.deleteBytes(indexFile, currentFilePointer, INDEX_KEY_ENTRY_SIZE);
                    break;
                }
            }
        }finally {
            this.lock.unlock();
        }
    }

    @Override
    public void close() throws Exception {
        this.indexFile.close();
    }
}
