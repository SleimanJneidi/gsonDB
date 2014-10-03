package gsonDB.document;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gsonDB.DB;
import gsonDB.LongKeyException;
import gsonDB.index.DefaultIndexProcessor;
import gsonDB.index.IndexProcessor;
import gsonDB.index.IndexKeyEntry;

/**
 * @author Sleiman
 */
public abstract class DocumentProcessor implements AutoCloseable{

    protected final RandomAccessFile dataFile;
    private final Class<?> entityType;

    protected final DB db;
    protected final Lock lock = new ReentrantLock();

    protected DocumentProcessor(final Class<?> entityType, DB db) throws FileNotFoundException {

        this.db = db;
        this.entityType = entityType;
        String documentFileName = entityType.getName() + "_data";
        File file = new File(db.getDbDir(), documentFileName);
        this.dataFile = new RandomAccessFile(file, "rw");

    }

    public static final DocumentProcessor getDocumentProcessor(final Class<?> entityType, DB db) throws FileNotFoundException {
        Preconditions.checkNotNull(entityType, "Entity type shouldn't be null");
        Preconditions.checkNotNull(db, "DB object shouldn't be null");

        return new DefaultDocumentProcessor(entityType, db);
    }

    public abstract void insert(Object object) throws IOException;

    public abstract <T> List<T> findAll(Class<T> entityType) throws IOException;

    public abstract <T> List<T> findAll(Class<T> entityType, JsonElement query) throws IOException;

    public abstract <T> T find(Class<T> entityType, String id) throws IOException;

    public abstract void delete(String id, Class<?> entityType) throws IOException;

    public abstract void update(String id, Object newValue) throws IOException;

    public DB getDb() {
        return db;
    }

    public Class<?> getEntityType() {
        return entityType;
    }
}