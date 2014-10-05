package gsonDB.document;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import gsonDB.DB;
import gsonDB.LongKeyException;
import gsonDB.index.DefaultIndexProcessor;
import gsonDB.index.IndexKeyEntry;
import gsonDB.index.IndexProcessor;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Sleiman on 02/10/2014.
 */

public class DefaultDocumentProcessor extends DocumentProcessor {

    public static final String DEFAULT_ID_NAME = "id";

    protected DefaultDocumentProcessor(Class<?> entityType, DB db) throws FileNotFoundException {
        super(entityType, db);
        // gson.getAdapter(entityType).re
    }

    @Override
    public JsonObject insert(Object object) throws IOException {

        Preconditions.checkNotNull(object, "Inserted object shouldn't be null");
        Gson gson = new Gson();
        JsonObject jsonObject = gson.toJsonTree(object).getAsJsonObject();
        JsonElement jsonIdElement = jsonObject.get(DEFAULT_ID_NAME);
        String id = null;
        if (jsonIdElement != null) {
            id = jsonIdElement.getAsString();
        }

        if (jsonIdElement != null && id.getBytes().length > DefaultIndexProcessor.KEY_SIZE) {
            throw new LongKeyException();
        } else if (jsonIdElement == null) { // generate key for the document
            id = UUID.randomUUID().toString();
            jsonObject.addProperty(DEFAULT_ID_NAME, id);
        }


        final byte[] jsonBuffer = jsonObject.toString().getBytes("UTF-8");

        try {
            this.lock.lock();

            final long filePointer = this.writeDocument(jsonBuffer);
            IndexProcessor indexProcessor = IndexProcessor.getIndexHandler(object.getClass(), db);
            IndexKeyEntry indexKeyEntry = new IndexKeyEntry(id, filePointer, jsonBuffer.length);
            indexProcessor.insertNewIndexEntry(indexKeyEntry);

        } finally {
            this.lock.unlock();
        }
        return jsonObject;
    }


    @Override
    public <T> List<T> findAll(Class<T> entityType) throws IOException {

        return this.find(entityType,new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return true;
            }
        });
    }

    @Override
    public <T> List<T> find(Class<T> entityType, Predicate<T> predicate) throws IOException {
        List<T> results = new ArrayList<>();

        try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            reader.beginArray();
            while (reader.hasNext()) {
                T object = gson.fromJson(reader, entityType);
                if(predicate.apply(object)) {
                    results.add(object);
                }
            }
            reader.endArray();
        }
        return results;
    }

    @Override
    public <T> T find(Class<T> entityType, String id) throws IOException {
        DefaultIndexProcessor indexProcessor = (DefaultIndexProcessor) DefaultIndexProcessor.getIndexHandler(entityType, db);
        IndexKeyEntry indexKeyEntry = indexProcessor.getIndexByKey(id);
        if (indexKeyEntry == null)
            return null;
        try {
            this.lock.lock();
            long filePointer = indexKeyEntry.getDataFilePointer();
            int recordSize = indexKeyEntry.getRecordSize();
            byte[] buffer = new byte[recordSize];
            dataFile.seek(filePointer);
            dataFile.readFully(buffer);
            String json = new String(buffer, "UTF-8");
            T object = gson.fromJson(json, entityType);
            return object;

        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void delete(String id, Class<?> entityType) {

    }

    @Override
    public void update(String id, Object newValue) {

    }

    protected long writeDocument(byte[] json) throws IOException {
        long filePointer;
        if (this.dataFile.length() == 0) { //empty file then  append array [
            dataFile.writeBytes("[");
            filePointer = this.dataFile.getFilePointer();

            this.dataFile.write(json);
            dataFile.writeBytes("]");
            return filePointer;
        } else {
            filePointer = dataFile.length() - 1; // look behind 2 bytes (size of char)
            dataFile.seek(filePointer);
            dataFile.writeBytes(",");
            filePointer = dataFile.getFilePointer();
            dataFile.write(json);
            dataFile.writeBytes("]");
            return filePointer;
        }
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
