package gsonDB.document;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gsonDB.DB;
import gsonDB.LongKeyException;
import gsonDB.index.DefaultIndexProcessor;
import gsonDB.index.IndexKeyEntry;
import gsonDB.index.IndexProcessor;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.UUID;

/**
 * Created by Sleiman on 02/10/2014.
 */

public class DefaultDocumentProcessor extends DocumentProcessor {

    public static final String DEFAULT_ID_NAME = "id";

    protected DefaultDocumentProcessor(Class<?> entityType, DB db) throws FileNotFoundException {
        super(entityType, db);
    }

    @Override
    public void insert(Object object) throws IOException {

        Preconditions.checkNotNull(object, "Inserted object shouldn't be null");
        Gson gson = new Gson();
        JsonObject jsonObject = gson.toJsonTree(object).getAsJsonObject();
        String id = jsonObject.get(DEFAULT_ID_NAME).getAsString();

        if (id != null && id.getBytes("UTF-8").length > DefaultIndexProcessor.KEY_SIZE) {
            throw new LongKeyException();
        } else { // generate key for the document
            id = UUID.randomUUID().toString();
            jsonObject.addProperty(DEFAULT_ID_NAME, id);
        }


        final byte[] jsonStringBytes = jsonObject.getAsString().getBytes("UTF-8");
        try {
            this.lock.lock();
            final long filePointer = this.writeDocument(jsonStringBytes);
            IndexProcessor indexHandler = IndexProcessor.getIndexHandler(object.getClass(), db);

        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public <T> List<T> findAll(Class<T> entityType) {
        return null;
    }

    @Override
    public <T> List<T> findAll(Class<T> entityType, JsonElement query) {
        return null;
    }

    @Override
    public <T> T find(Class<T> entityType, String id) {
        return null;
    }

    @Override
    public void delete(String id, Class<?> entityType) {

    }

    @Override
    public void update(String id, Object newValue) {

    }

    protected long writeDocument(byte[] data) throws IOException {
        long newEntryFilePointer = this.dataFile.length(); // EOF
        this.dataFile.seek(newEntryFilePointer);
        this.dataFile.write(data);
        this.dataFile.getChannel();
        return newEntryFilePointer;
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
        long readPointer = indexKeyEntry.getDataFilePointer() + indexKeyEntry.getRecordSize();
        long writePointer = indexKeyEntry.getDataFilePointer();

        try (FileChannel inChannel = this.dataFile.getChannel()) {
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


    @Override
    public void close() throws Exception {
        this.dataFile.close();
    }
}
