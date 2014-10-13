package gsonDB.document;

import com.google.gson.*;
import gsonDB.DB;
import gsonDB.index.IndexKeyEntry;

import java.io.*;

/**
 * Created by Sleiman on 02/10/2014.
 */

public class DefaultDocumentProcessor extends BasicDocumentProcessor {

    protected DefaultDocumentProcessor(Class<?> entityType, DB db) throws FileNotFoundException {
        super(entityType, db);
    }

    @Override
    public JsonObject insert(Object object) throws IOException {

        JsonObject jsonObject = toJson(object);
        long id = jsonObject.get(DEFAULT_ID_NAME).getAsLong();
        final byte[] jsonBuffer = jsonObject.toString().getBytes("UTF-8");

        try {
            this.lock.writeLock().lock();

            final long filePointer = this.writeDocument(jsonBuffer);
            IndexKeyEntry indexKeyEntry = new IndexKeyEntry(id, filePointer, jsonBuffer.length);
            indexProcessor.insertNewIndexEntry(indexKeyEntry);

        } finally {
            this.lock.writeLock().unlock();
        }
        return jsonObject;
    }

    @Override
    public void close() throws Exception {
        this.dataFile.close();
    }
}
