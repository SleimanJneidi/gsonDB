package gsonDB.document;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.gson.*;
import gsonDB.DB;
import gsonDB.LongKeyException;
import gsonDB.index.DefaultIndexProcessor;
import gsonDB.index.IndexKeyEntry;
import gsonDB.index.IndexProcessor;

import java.io.*;
import java.util.List;
import java.util.UUID;

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
        String id = jsonObject.get("id").getAsString();
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
    public void close() throws Exception {
        this.dataFile.close();
    }
}
