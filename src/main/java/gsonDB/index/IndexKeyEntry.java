package gsonDB.index;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import gsonDB.LongKeyException;

import java.io.Serializable;

/**
 * Created by Sleiman on 28/09/2014.
 */

public final class IndexKeyEntry implements Serializable {

    private final long key;
    private final long dataFilePointer;
    private final int recordSize;
    private final long indexEntryFilePointer;


    public IndexKeyEntry(final long key, final long dataFilePointer, final int recordSize) {
        this(key, dataFilePointer, recordSize, -1);
    }

    public IndexKeyEntry(final long key, final long dataFilePointer, final int recordSize, final long indexEntryFilePointer) {

        Preconditions.checkNotNull(key, "Key cannot be null");
        Preconditions.checkArgument(dataFilePointer >= 0);
        Preconditions.checkArgument(recordSize > 0);
        Preconditions.checkArgument(key > 0);

        this.key = key;
        this.dataFilePointer = dataFilePointer;
        this.recordSize = recordSize;
        this.indexEntryFilePointer = indexEntryFilePointer;
    }

    public long getKey() {
        return key;
    }

    public long getDataFilePointer() {
        return dataFilePointer;
    }

    public int getRecordSize() {
        return recordSize;
    }

    public IndexKeyEntry setIndexEntryFilePointer(long indexEntryFilePointer) {
        return new IndexKeyEntry(this.key, this.dataFilePointer, this.recordSize, indexEntryFilePointer);
    }

    public IndexKeyEntry setRecordSize(int recordSize) {
        return new IndexKeyEntry(this.key, this.dataFilePointer, recordSize, this.indexEntryFilePointer);
    }

    public IndexKeyEntry setDataFilePointer(long dataFilePointer) {
        return new IndexKeyEntry(this.key, dataFilePointer, this.recordSize, this.indexEntryFilePointer);
    }

    public long getIndexEntryFilePointer() {
        return indexEntryFilePointer;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("key", key)
                .add("data file pointer", dataFilePointer)
                .add("record size", recordSize)
                .add("index file pointer", indexEntryFilePointer)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.key, this.dataFilePointer, this.recordSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexKeyEntry that = (IndexKeyEntry) o;

        if (dataFilePointer != that.dataFilePointer) return false;
        if (key != that.key) return false;
        if (recordSize != that.recordSize) return false;

        return true;
    }
}
