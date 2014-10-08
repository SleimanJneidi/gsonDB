package gsonDB.document;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import gsonDB.DB;
import gsonDB.LongKeyException;
import gsonDB.index.DefaultIndexProcessor;
import gsonDB.index.IndexKeyEntry;
import gsonDB.index.IndexProcessor;
import gsonDB.utils.FileUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sleiman on 02/10/2014.
 */

public class DefaultDocumentProcessor extends BasicDocumentProcessor {

    public static final String DEFAULT_ID_NAME = "id";

    protected DefaultDocumentProcessor(Class<?> entityType, DB db) throws FileNotFoundException {
        super(entityType, db);
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
        return super.findAll(entityType);
    }

    @Override
    public <T> List<T> find(Class<T> entityType, Predicate<T> predicate) throws IOException {
        return super.find(entityType, predicate);
    }

    @Override
    public <T> T find(Class<T> entityType, String id) throws IOException {
        return super.find(entityType,id);
    }


    @Override
    public void delete(Class<?> entityType, String id) throws IOException {
        super.delete(entityType,id);
    }

    @Override
    public void update(String id, Object newValue) throws IOException {
        DefaultIndexProcessor indexProcessor = (DefaultIndexProcessor) DefaultIndexProcessor.getIndexHandler(entityType, db);
        IndexKeyEntry indexKeyEntry = indexProcessor.getIndexByKey(id);
        if (indexKeyEntry == null) {
            return;
        }
    }

    @Override
    public void close() throws Exception {
        this.dataFile.close();
    }
}
