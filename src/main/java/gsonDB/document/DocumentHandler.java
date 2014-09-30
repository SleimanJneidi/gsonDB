package gsonDB.document;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import gsonDB.DB;
import gsonDB.index.IndexKeyEntry;

public class DocumentHandler {
    private final RandomAccessFile dataFile;
    private final Class<?> entityType;
    private final DB db;

    private DocumentHandler(final Class<?> entityType, DB db) throws FileNotFoundException {
        this.db = db;
        this.entityType = entityType;
        String documentFileName = entityType.getName() + "_data.db";
        File file = new File(db.getDbDir(),documentFileName);
        this.dataFile = new RandomAccessFile(file, "rw");
    }

    public static final DocumentHandler getDocumentWriter(final Class<?> entityType, DB db) throws FileNotFoundException {
        return new DocumentHandler(entityType, db);
    }

    protected long writeDocument(byte[] data) throws IOException {
        long newEntryFilePointer = this.dataFile.length(); // EOF
        this.dataFile.seek(newEntryFilePointer);
        this.dataFile.write(data);
        this.dataFile.getChannel();
        return newEntryFilePointer;
    }

    public RandomAccessFile getDataFile() {
        return dataFile;
    }

    public DB getDb() {
        return db;
    }

    public byte[] readRecordEntry(IndexKeyEntry indexKeyEntry) throws IOException {
        if (indexKeyEntry == null) {
            throw new IllegalArgumentException("Index key cannot be null");
        }
        long filePointer = indexKeyEntry.getDataFilePointer();
        this.dataFile.seek(filePointer);
        byte[] buffer = new byte[indexKeyEntry.getRecordSize()];
        this.dataFile.read(buffer);
        return buffer;
    }

    public void deleteRecordEntry(IndexKeyEntry indexKeyEntry) throws IOException {
        if (indexKeyEntry == null) {
            throw new IllegalArgumentException("Index key cannot be null");
        }

        final int BUFFER_SIZE = 128;
        long readPointer =indexKeyEntry.getDataFilePointer()+ indexKeyEntry.getRecordSize();
        long writePointer = indexKeyEntry.getDataFilePointer();

        try(FileChannel inChannel = this.dataFile.getChannel()) {
            inChannel.position(readPointer);

            ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
            int bytesRead = inChannel.read(buf);
            while (bytesRead != -1) {

                inChannel.position(writePointer);
                inChannel.write(buf);

                readPointer += BUFFER_SIZE;
                writePointer += BUFFER_SIZE;
                buf.clear();
                inChannel.position(readPointer);
                bytesRead = inChannel.read(buf);
            }
        }
    }
}