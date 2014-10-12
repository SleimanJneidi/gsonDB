package gsonDB.document;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import gsonDB.DB;
import gsonDB.LongKeyException;
import gsonDB.index.DefaultIndexProcessor;
import gsonDB.index.IndexKeyEntry;
import gsonDB.index.IndexProcessor;
import gsonDB.utils.FileUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Sleiman on 08/10/2014.
 */
public abstract class BasicDocumentProcessor extends DocumentProcessor {

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    protected BasicDocumentProcessor(Class<?> entityType, DB db) throws FileNotFoundException {
        super(entityType, db);
    }


    @Override
    public <T> T find(String id, Class<T> entityType) throws IOException {
        Optional<IndexKeyEntry> indexKeyEntryOptional = indexProcessor.getIndexByKey(id);
        if (!indexKeyEntryOptional.isPresent()) {
            return null;
        }
        try {
            this.lock.readLock().lock();

            IndexKeyEntry indexKeyEntry = indexKeyEntryOptional.get();

            long filePointer = indexKeyEntry.getDataFilePointer();
            int recordSize = indexKeyEntry.getRecordSize();
            byte[] buffer = new byte[recordSize];
            dataFile.seek(filePointer);
            dataFile.readFully(buffer);
            String json = new String(buffer, "UTF-8");
            T object = gson.fromJson(json, entityType);
            return object;

        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public <T> List<T> findAll(Class<T> entityType) throws IOException {

        return this.find(entityType, new Predicate<T>() {
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
            reader.setLenient(true);
            while (reader.peek() != JsonToken.END_DOCUMENT) {
                T object = gson.fromJson(reader, entityType);
                if (predicate.apply(object)) {
                    results.add(object);
                }
            }
        }
        return results;
    }

    @Override
    public void update(String id, Object newValue) throws IOException {
        /*
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(newValue);
        Preconditions.checkArgument(newValue.getClass().equals(entityType));

        IndexProcessor indexProcessor = IndexProcessor.getIndexProcessor(entityType,db);
        final Optional<IndexKeyEntry> indexByKeyOptional = indexProcessor.getIndexByKey(id);
        Preconditions.checkArgument(indexByKeyOptional.isPresent(), String.format(" Element with id: %s not found", id));

        IndexKeyEntry oldRecordIndexEntry = indexByKeyOptional.get();
        */

        throw new NotImplementedException();
    }

    @Override
    public void delete(String id, Class<?> entityType) throws IOException {
        Optional<IndexKeyEntry> indexKeyEntryOptional = indexProcessor.getIndexByKey(id);

        Preconditions.checkArgument(indexKeyEntryOptional.isPresent(),"Id not found");
        try {
            this.lock.writeLock().lock();
            deleteRecord(indexKeyEntryOptional.get());
        }finally {
            this.lock.writeLock().unlock();
        }
    }

    protected long writeDocument(byte[] buffer) throws IOException {
        long filePointer;
        dataFile.seek(dataFile.length());
        filePointer = dataFile.getFilePointer();
        dataFile.write(buffer);
        return filePointer;
    }

    protected void deleteRecord(IndexKeyEntry indexKeyEntry) throws IOException {
        FileUtils.deleteBytes(dataFile, indexKeyEntry.getDataFilePointer(), indexKeyEntry.getRecordSize());
        indexProcessor.deleteIndexKeyEntry(indexKeyEntry);
    }

    protected JsonObject toJson(Object object) {
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
            id = String.valueOf(System.nanoTime());
            jsonObject.addProperty(DEFAULT_ID_NAME, id);
        }
        return jsonObject;
    }
}
