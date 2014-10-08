package gsonDB.index;

import com.google.common.base.Preconditions;
import gsonDB.DB;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sleiman on 28/09/2014.
 */
public abstract class IndexProcessor implements AutoCloseable{

    private static final Map<Class<?>,IndexProcessor> INDEX_HANDLERS = new ConcurrentHashMap<>();

    protected final DB db;
    protected final Class<?> entityType;
    protected final File file;
    protected final RandomAccessFile indexFile;

    public static IndexProcessor getIndexHandler(final Class<?> type,final DB db) throws FileNotFoundException {
        Preconditions.checkNotNull(type,"Class shouldn't be null");
        Preconditions.checkNotNull(db,"DB is null");

        if (INDEX_HANDLERS.containsKey(type)) {
            return INDEX_HANDLERS.get(type);
        }
        IndexProcessor newHandler = new DefaultIndexProcessor(type,db); // FIXME: use default for now
        INDEX_HANDLERS.put(type, newHandler);
        return newHandler;
    }

    protected IndexProcessor(final Class<?> entityType, final DB db) throws FileNotFoundException {
        this.db = db;
        this.entityType = entityType;
        final String indexFileName = entityType.getSimpleName()+ "_index";
        this.file = new File(db.getDbDir(),indexFileName);
        this.indexFile = new RandomAccessFile(this.file,"rw");
    }

    public abstract int count() throws IOException;

    public abstract void insertNewIndexEntry(IndexKeyEntry indexKeyEntry) throws IOException;

    public static Map<Class<?>, IndexProcessor> allIndexHandlers(){
        return Collections.unmodifiableMap(INDEX_HANDLERS);
    }

    public DB getDb() {
        return db;
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    protected File getFile() {
        return file;
    }


}
