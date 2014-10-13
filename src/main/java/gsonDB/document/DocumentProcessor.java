package gsonDB.document;

import java.io.*;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gsonDB.DB;
import gsonDB.index.IndexProcessor;

/**
 * @author Sleiman
 */
public abstract class DocumentProcessor implements AutoCloseable {

    public static final String DEFAULT_ID_NAME = "_id";

    protected final RandomAccessFile dataFile;

    protected final File file;

    protected final Class<?> entityType;

    protected final DB db;

    protected final Gson gson = new Gson();

    protected final IndexProcessor indexProcessor;

    protected DocumentProcessor(final Class<?> entityType, DB db) throws FileNotFoundException {

        this.db = db;
        this.entityType = entityType;
        String documentFileName = entityType.getSimpleName() + "_data";
        this.file = new File(db.getDBDir(), documentFileName);
        this.dataFile = new RandomAccessFile(file, "rw");
        indexProcessor = IndexProcessor.getIndexProcessor(entityType, db);

    }

    public static DocumentProcessor getDocumentProcessor(final Class<?> entityType,final DB db) throws FileNotFoundException {
        Preconditions.checkNotNull(entityType, "Entity type shouldn't be null");
        Preconditions.checkNotNull(db, "DB object shouldn't be null");

        return new DefaultDocumentProcessor(entityType, db);
    }

    /**
     *
     * @param object POJO to be persisted
     * @return JsonObject
     * @throws IOException
     */
    public abstract JsonObject insert(Object object) throws IOException;

    public abstract <T> List<T> findAll(Class<T> entityType) throws IOException;

    public abstract <T> List<T> find(Class<T> entityType, Predicate<T> predicate) throws IOException;

    public abstract <T> Optional<T> find(long id, Class<T> entityType) throws IOException;

    public abstract void delete(long id, Class<?> entityType) throws IOException;

    public abstract void update(long id, Object newValue) throws IOException;

    public DB getDb() {
        return db;
    }

    public Class<?> getEntityType() {
        return entityType;
    }
}