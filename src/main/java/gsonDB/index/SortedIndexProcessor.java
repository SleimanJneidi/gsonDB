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

        final byte[] keyBufferArray = keyByteBuffer.array();
        final byte[] dataFilePointerArray = dataFilePointerBuffer.array();
        final byte[] recordSizeArray = recordSizeBuffer.array();

        final byte[] joinedArray = new byte[keyBufferArray.length + dataFilePointerArray.length + recordSizeArray.length];

        System.arraycopy(keyBufferArray, 0, joinedArray, 0, keyBufferArray.length);
        System.arraycopy(dataFilePointerArray, 0, joinedArray, keyBufferArray.length, dataFilePointerArray.length);
        System.arraycopy(recordSizeArray, 0, joinedArray, dataFilePointerArray.length, recordSizeArray.length);

        final ByteBuffer newIndexEntryBuffer = ByteBuffer.wrap(joinedArray);

        FileUtils.pushBuffer(indexFile, newIndexEntryBuffer, greaterThanInput.getIndexEntryFilePointer());

    }

    private IndexKeyEntry lastIndexEntry() throws IOException {
        return indexKeyEntryAtFilePosition(indexFile.length() - INDEX_KEY_ENTRY_SIZE).get();
    }

    private void writeAt(IndexKeyEntry indexKeyEntry, long position) throws IOException {

        this.indexFile.seek(position);
        ByteBuffer keyByteBuffer = ByteBuffer.allocate(KEY_SIZE).put(indexKeyEntry.getKey().getBytes());
        byte[] keyBuffer = keyByteBuffer.array();

        this.indexFile.write(keyBuffer);
        this.indexFile.writeLong(indexKeyEntry.getDataFilePointer());
        this.indexFile.writeInt(indexKeyEntry.getRecordSize());
    }
}
