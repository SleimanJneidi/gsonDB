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
    public static final int KEY_SIZE = 8; // long value
    protected static final int FILE_POINTER_SIZE = 8; // long value
    public static final int RECORD_LENGTH_SIZE = 4; // integer value
    protected static final int INDEX_KEY_ENTRY_SIZE = KEY_SIZE + FILE_POINTER_SIZE + RECORD_LENGTH_SIZE;

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
                try {
                    long indexFilePointer = indexFile.getFilePointer();
                    long key = indexFile.readLong();
                    long recordFilePointer = indexFile.readLong();
                    int recordSize = indexFile.readInt();
                    indexKeyEntry = new IndexKeyEntry(key, recordFilePointer, recordSize, indexFilePointer);
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
            writeAt(indexKeyEntry, this.indexFile.length());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<IndexKeyEntry> getIndexByKey(long key) throws IOException {
        Preconditions.checkNotNull(key);
        Optional<IndexKeyEntry> indexKeyEntry = tryFindIndex(key);
        return indexKeyEntry;
    }

    @Override
    public IndexKeyEntry updateIndexKeyEntry(final IndexKeyEntry newIndexKeyEntry) throws IOException {
        Preconditions.checkNotNull(newIndexKeyEntry);
        Preconditions.checkNotNull(newIndexKeyEntry.getKey());

        Optional<IndexKeyEntry> oldIndexKeyEntryOptional = tryFindIndex(newIndexKeyEntry.getKey());
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

            final Optional<IndexKeyEntry> resultOptional = tryFindIndex(indexKeyEntry.getKey());
            if (resultOptional.isPresent()) {
                IndexKeyEntry result = resultOptional.get();
                FileUtils.deleteBytes(indexFile, result.getIndexEntryFilePointer(), INDEX_KEY_ENTRY_SIZE);
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void close() throws Exception {
        this.indexFile.close();
    }


    private Optional<IndexKeyEntry> tryFindIndex(final long key) {

        final Optional<IndexKeyEntry> result = Iterables.tryFind(indexKeyEntryIterable(), new Predicate<IndexKeyEntry>() {
            @Override
            public boolean apply(IndexKeyEntry input) {
                return input.getKey() == key;
            }
        });
        return result;
    }

    protected class IndexKeyEntryIterator implements Iterator<IndexKeyEntry> {
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

    public Iterable<IndexKeyEntry> indexKeyEntryIterable() {
        return new Iterable<IndexKeyEntry>() {
            @Override
            public Iterator<IndexKeyEntry> iterator() {
                return new IndexKeyEntryIterator();
            }
        };
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
}
