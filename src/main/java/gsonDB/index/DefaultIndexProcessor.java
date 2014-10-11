package gsonDB.index;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
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
public class DefaultIndexProcessor extends IndexProcessor {

    private final Lock lock = new ReentrantLock();
    private static final int KEY_TABLE_FILE_POINTER = 0; // 4 bytes after the number of records
    public static final int KEY_SIZE = 36; // 36 bytes
    protected static final int FILE_POINTER_SIZE = 8; // long value
    private static final int RECORD_LENGTH_SIZE = 4; // integer value
    private static final int INDEX_KEY_ENTRY_SIZE = KEY_SIZE + FILE_POINTER_SIZE + RECORD_LENGTH_SIZE;

    protected DefaultIndexProcessor(final Class<?> entityType, final DB db) throws FileNotFoundException {
        super(entityType, db);
    }


    @Override
    public long count() throws IOException {
        return indexFile.length() / INDEX_KEY_ENTRY_SIZE;
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

    @Override
    public void insertNewIndexEntry(IndexKeyEntry indexKeyEntry) throws IOException {
        Preconditions.checkNotNull(indexKeyEntry);
        try {
            lock.lock();
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

    @Override
    public IndexKeyEntry getIndexByKey(String key) throws IOException {
        Preconditions.checkNotNull(key);
        Optional<IndexKeyEntry> indexKeyEntry = tryFindIndex(key);
        return indexKeyEntry.isPresent() ? indexKeyEntry.get() : null;
    }

    @Override
    public IndexKeyEntry updateIndexKeyEntry(final IndexKeyEntry newIndexKeyEntry) throws IOException {
        Preconditions.checkNotNull(newIndexKeyEntry);
        Preconditions.checkNotNull(newIndexKeyEntry.getKey());

        Optional<IndexKeyEntry> oldIndexKeyEntry = tryFindIndex(newIndexKeyEntry.getKey());
        if (!oldIndexKeyEntry.isPresent()) {
            throw new RuntimeException("Index key entry does not exist");
        }
        return oldIndexKeyEntry.get();
    }

    @Override
    public void deleteIndexKeyEntry(IndexKeyEntry indexKeyEntry) throws IOException {
        try {
            this.lock.lock();
            long currentCount = count();
            indexFile.seek(KEY_TABLE_FILE_POINTER);
            long currentFilePointer;
            while ((currentFilePointer = indexFile.getFilePointer()) <= (indexFile.length() + INDEX_KEY_ENTRY_SIZE)) {
                IndexKeyEntry fetchedIndexedKeyEntry = fetchNextIndexKeyEntry().get();
                if (indexKeyEntry.equals(fetchedIndexedKeyEntry)) {
                    FileUtils.deleteBytes(indexFile, currentFilePointer, INDEX_KEY_ENTRY_SIZE);
                    break;
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void close() throws Exception {
        this.indexFile.close();
    }


    private Optional<IndexKeyEntry> tryFindIndex(final String key) {

        final Optional<IndexKeyEntry> result = Iterables.tryFind(new Iterable<IndexKeyEntry>() {
            @Override
            public Iterator<IndexKeyEntry> iterator() {
                return new IndexKeyEntryIterator();
            }
        }, new Predicate<IndexKeyEntry>() {
            @Override
            public boolean apply(IndexKeyEntry input) {
                return input.getKey().equals(key);
            }
        });
        return result;
    }

    private class IndexKeyEntryIterator implements Iterator<IndexKeyEntry> {
        int current = 0;
        long size;

        IndexKeyEntryIterator() {
            try {
                size = count();
                indexFile.seek(KEY_TABLE_FILE_POINTER);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean hasNext() {
            return current < size;
        }

        @Override
        public IndexKeyEntry next() {
            IndexKeyEntry next = fetchNextIndexKeyEntry().get();
            current++;
            return next;
        }

        @Override
        public void remove() {
            throw new NotImplementedException();
        }
    }

}
