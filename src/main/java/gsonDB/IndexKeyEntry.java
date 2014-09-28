package gsonDB;

/**
 * Created by Sleiman on 28/09/2014.
 */

class IndexKeyEntry {

    private final String key;
    private final long dataFilePointer;
    private final int recordSize;

    protected IndexKeyEntry(final String key,final long dataFilePointer,final int recordSize){
        this.key = key;
        this.dataFilePointer = dataFilePointer;
        this.recordSize = recordSize;
    }

    public String getKey() {
        return key;
    }

    public long getDataFilePointer() {
        return dataFilePointer;
    }

    public int getRecordSize() {
        return recordSize;
    }
}
