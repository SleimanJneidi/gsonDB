package gsonDB;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;
import gsonDB.document.DocumentProcessor;
import gsonDB.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Sleiman on 28/09/2014.
 */

public class GsonDB implements DB {

    private final File dbDir;

    public static DB getDB(final File dbDir) {
        Preconditions.checkNotNull(dbDir, "DB directory file object shouldn't be null");
        Preconditions.checkArgument(dbDir.isDirectory(), "DB file should be a valid directory");
        return new GsonDB(dbDir);
    }

    private GsonDB(File dbDir) {
        this.dbDir = dbDir;
    }


    @Override
    public File getDBDir() {
        return dbDir;
    }

    @Override
    public <T> List<T> findAll(final Class<T> clazz) {
        try (DocumentProcessor documentProcessor = DocumentProcessor.getDocumentProcessor(clazz, this)) {
            return documentProcessor.findAll(clazz);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public <T> List<T> find(final Class<T> clazz, final Predicate<T> filter) {
        try (DocumentProcessor documentProcessor = DocumentProcessor.getDocumentProcessor(clazz, this)) {
            return documentProcessor.find(clazz, filter);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public long size() {
        return FileUtils.directorySize(this.dbDir);
    }

}
