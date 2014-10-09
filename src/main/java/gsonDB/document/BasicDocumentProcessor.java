package gsonDB.document;

import com.google.common.base.Predicate;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import gsonDB.DB;
import gsonDB.index.DefaultIndexProcessor;
import gsonDB.index.IndexKeyEntry;
import gsonDB.index.IndexProcessor;
import gsonDB.utils.FileUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sleiman on 08/10/2014.
 */
public abstract class BasicDocumentProcessor extends DocumentProcessor{

    protected final Lock lock = new ReentrantLock();

    protected BasicDocumentProcessor(Class<?> entityType, DB db) throws FileNotFoundException {
        super(entityType, db);
    }


    @Override
    public <T> T find(String id, Class<T> entityType) throws IOException {
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
        DefaultIndexProcessor indexProcessor = (DefaultIndexProcessor) DefaultIndexProcessor.getIndexHandler(entityType, db);
        IndexKeyEntry indexKeyEntry = indexProcessor.getIndexByKey(id);
        if (indexKeyEntry == null) {
            return;
        }
    }

    @Override
    public void delete(String id, Class<?> entityType) throws IOException {
        IndexProcessor indexProcessor = IndexProcessor.getIndexHandler(entityType, db);
        IndexKeyEntry indexKeyEntry = indexProcessor.getIndexByKey(id);
        if (indexKeyEntry == null) {
            return;
        }
        deleteRecord(indexProcessor,indexKeyEntry);
    }

    protected long writeDocument(byte[] json) throws IOException {
        long filePointer;
        dataFile.seek(dataFile.length());
        filePointer = dataFile.getFilePointer();
        dataFile.write(json);
        return filePointer;
    }

    protected void deleteRecord(IndexProcessor indexProcessor, IndexKeyEntry indexKeyEntry) throws IOException{
        FileUtils.deleteBytes(dataFile, indexKeyEntry.getDataFilePointer(), indexKeyEntry.getRecordSize());
        indexProcessor.deleteIndexKeyEntry(indexKeyEntry);
    }
}
