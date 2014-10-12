package gsonDB.index;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.ObjectArrays;
import com.sun.deploy.util.ArrayUtil;
import gsonDB.DB;
import gsonDB.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;

/**
 * Created by Sleiman on 11/10/2014.
 */
public class SortedIndexProcessor extends DefaultIndexProcessor {

    protected SortedIndexProcessor(Class<?> entityType, DB db) throws FileNotFoundException {
        super(entityType, db);
    }

    @Override
    public void insertNewIndexEntry(final IndexKeyEntry indexKeyEntry) throws IOException {

        Preconditions.checkNotNull(indexKeyEntry);
        if (indexFile.length() == 0 || (indexKeyEntry.getKey().compareTo(lastIndexEntry().getKey()) > 0)) {
            writeAt(indexKeyEntry, indexFile.length());
            return;
        }
        final IndexKeyEntry greaterThanInput = Iterables.tryFind(new Iterable<IndexKeyEntry>() {
            @Override
            public Iterator<IndexKeyEntry> iterator() {
                return new IndexKeyEntryIterator();
            }
        }, new Predicate<IndexKeyEntry>() {
            @Override
            public boolean apply(IndexKeyEntry input) {
                return input.getKey().compareTo(indexKeyEntry.getKey()) > 0;
            }
        }).get();

        ByteBuffer keyByteBuffer = ByteBuffer.allocate(KEY_SIZE).put(indexKeyEntry.getKey().getBytes());
        ByteBuffer dataFilePointerBuffer = ByteBuffer.allocate(FILE_POINTER_SIZE).putLong(indexKeyEntry.getDataFilePointer());
        ByteBuffer recordSizeBuffer = ByteBuffer.allocate(RECORD_LENGTH_SIZE).putInt(indexKeyEntry.getRecordSize());

        final ByteBuffer newIndexEntryBuffer = FileUtils.join(keyByteBuffer, dataFilePointerBuffer, recordSizeBuffer);

        FileUtils.pushBuffer(indexFile, newIndexEntryBuffer, greaterThanInput.getIndexEntryFilePointer());

    }

    private IndexKeyEntry lastIndexEntry() throws IOException {
        return indexKeyEntryAtFilePosition(indexFile.length() - INDEX_KEY_ENTRY_SIZE).get();
    }

    Optional<IndexKeyEntry> find(String key) throws IOException {
        Preconditions.checkNotNull(key);
        if (indexFile.length() == 0 || key.compareTo(lastIndexEntry().getKey()) > 0) {
            return Optional.absent();
        }
        long start = 0;
        long end = indexFile.length();
        return binarySearch(key, start, end);

    }

    private Optional<IndexKeyEntry> binarySearch(String key, long start, long end) throws IOException {

        long mid = (start + end) / 2;
        if (mid < INDEX_KEY_ENTRY_SIZE) {
            mid = 0;
        }
        if (mid == indexFile.length()) {
            mid = mid - INDEX_KEY_ENTRY_SIZE;
        }

        if (mid % INDEX_KEY_ENTRY_SIZE != 0) {
            mid = mid + mid % INDEX_KEY_ENTRY_SIZE;
        }

        IndexKeyEntry indexKeyEntry = indexAt(mid);
        if (indexKeyEntry.getKey().compareTo(key) == 0) {
            return Optional.of(indexKeyEntry);
        } else if (mid == start || mid == end) {
            return Optional.absent();
        } else if (indexKeyEntry.getKey().compareTo(key) > 0) {
            return binarySearch(key, start, mid);
        } else {
            return binarySearch(key, mid, end);
        }
    }

    IndexKeyEntry indexAt(long position) throws IOException {
        Preconditions.checkArgument(position <= indexFile.length() + KEY_SIZE);

        indexFile.seek(position);
        byte[] keyBuffer = new byte[KEY_SIZE];

        long indexFilePointer = indexFile.getFilePointer();
        indexFile.readFully(keyBuffer);
        String key = new String(keyBuffer);
        key = key.trim(); // the key is often less than 36 bytes
        long recordFilePointer = indexFile.readLong();
        int recordSize = indexFile.readInt();
        IndexKeyEntry indexKeyEntry = new IndexKeyEntry(key, recordFilePointer, recordSize, indexFilePointer);

        return indexKeyEntry;
    }

}
