package gsonDB.index;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.UnmodifiableIterator;
import gsonDB.DB;
import gsonDB.utils.FileUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sleiman on 03/10/2014.
 */
public class PrimaryIndexProcessor extends IndexProcessor {

    private final Lock lock = new ReentrantLock();
    public static final int KEY_SIZE = 8; // long value
    protected static final int FILE_POINTER_SIZE = 8; // long value
    public static final int RECORD_LENGTH_SIZE = 4; // integer value
    protected static final int INDEX_KEY_ENTRY_SIZE = KEY_SIZE + FILE_POINTER_SIZE + RECORD_LENGTH_SIZE;

    protected PrimaryIndexProcessor(final Class<?> entityType, final DB db) throws FileNotFoundException {
        super(entityType, db);
    }


    @Override
    public long count() throws IOException {
        return indexFile.length() / INDEX_KEY_ENTRY_SIZE;
    }

    @Override
    public void insertNewIndexEntry(IndexKeyEntry indexKeyEntry) throws IOException {
        Preconditions.checkNotNull(indexKeyEntry);
        try {
            lock.lock();
            this.indexFile.seek(this.indexFile.length());
            writeAt(indexKeyEntry, this.indexFile.length());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public IndexKeyEntry updateIndexKeyEntry(final IndexKeyEntry newIndexKeyEntry) throws IOException {
        Preconditions.checkNotNull(newIndexKeyEntry);
        Preconditions.checkNotNull(newIndexKeyEntry.getKey());

        Optional<IndexKeyEntry> oldIndexKeyEntryOptional = getIndexByKey(newIndexKeyEntry.getKey());
        Preconditions.checkArgument(oldIndexKeyEntryOptional.isPresent(), "Index key entry does not exist");

        IndexKeyEntry oldIndexKeyEntry = oldIndexKeyEntryOptional.get();
        // write record length and file pointer
        indexFile.seek(oldIndexKeyEntry.getIndexEntryFilePointer() + KEY_SIZE);
        indexFile.writeLong(newIndexKeyEntry.getDataFilePointer());
        indexFile.writeInt(newIndexKeyEntry.getRecordSize());

        return newIndexKeyEntry.setIndexEntryFilePointer(oldIndexKeyEntry.getIndexEntryFilePointer());
    }

    @Override
    public void deleteIndexKeyEntry(IndexKeyEntry indexKeyEntry) throws IOException {
        try {
            this.lock.lock();

            final Optional<IndexKeyEntry> resultOptional = getIndexByKey(indexKeyEntry.getKey());
            if (resultOptional.isPresent()) {
                IndexKeyEntry result = resultOptional.get();
                FileUtils.deleteBytes(indexFile, result.getIndexEntryFilePointer(), INDEX_KEY_ENTRY_SIZE);
            }
        } finally {
            this.lock.unlock();
        }
    }

    protected Optional<IndexKeyEntry> indexKeyEntryAtFilePosition(long filePosition) throws IOException {
        Preconditions.checkArgument(filePosition >= 0);

        if (filePosition + INDEX_KEY_ENTRY_SIZE > indexFile.length()) {
            return Optional.absent();
        }

        indexFile.seek(filePosition);

        long indexFilePointer = indexFile.getFilePointer();
        long key = indexFile.readLong();
        long recordFilePointer = indexFile.readLong();
        int recordSize = indexFile.readInt();
        IndexKeyEntry indexKeyEntry = new IndexKeyEntry(key, recordFilePointer, recordSize, indexFilePointer);
        return Optional.of(indexKeyEntry);

    }

    protected void writeAt(IndexKeyEntry indexKeyEntry, long position) throws IOException {

        this.indexFile.seek(position);
        this.indexFile.writeLong(indexKeyEntry.getKey());
        this.indexFile.writeLong(indexKeyEntry.getDataFilePointer());
        this.indexFile.writeInt(indexKeyEntry.getRecordSize());
    }

    @Override
    public Optional<IndexKeyEntry> getIndexByKey(long key) throws IOException {
        Preconditions.checkNotNull(key);
        if (indexFile.length() == 0 || key > lastIndexEntry().getKey()) {
            return Optional.absent();
        }
        long start = 0;
        long end = indexFile.length();
        return binarySearch(key, start, end);

    }

    private Optional<IndexKeyEntry> binarySearch(long key, long start, long end) throws IOException {

        long mid = (start + end) / 2;
        if (mid < INDEX_KEY_ENTRY_SIZE) {
            mid = 0;
        }
        if (mid == indexFile.length()) {
            mid = mid - INDEX_KEY_ENTRY_SIZE;
        }

        mid = mid + mid % INDEX_KEY_ENTRY_SIZE; // adapt the mid to a valid key entry


        IndexKeyEntry indexKeyEntry = indexAt(mid);
        if (indexKeyEntry.getKey() == key) {
            return Optional.of(indexKeyEntry);
        } else if (mid == start || mid == end) {
            return Optional.absent();
        } else if (indexKeyEntry.getKey() > key) {
            return binarySearch(key, start, mid);
        } else {
            return binarySearch(key, mid, end);
        }
    }

    private IndexKeyEntry indexAt(long position) throws IOException {
        Preconditions.checkArgument(position <= indexFile.length() + KEY_SIZE);

        indexFile.seek(position);

        long indexFilePointer = indexFile.getFilePointer();
        long key = indexFile.readLong();
        long recordFilePointer = indexFile.readLong();
        int recordSize = indexFile.readInt();

        IndexKeyEntry indexKeyEntry = new IndexKeyEntry(key, recordFilePointer, recordSize, indexFilePointer);

        return indexKeyEntry;
    }

    private IndexKeyEntry lastIndexEntry() throws IOException {
        return indexKeyEntryAtFilePosition(indexFile.length() - INDEX_KEY_ENTRY_SIZE).get();
    }

    @Override
    public void close() throws Exception {
        this.indexFile.close();
    }

}
