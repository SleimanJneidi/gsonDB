package gsonDB;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.Set;

/**
 * Created by Sleiman on 28/09/2014.
 */
public class DB {

    private final File dbDir;

    public static DB getDB(final File dbDir) {
        Preconditions.checkNotNull(dbDir,"DB directory file object shouldn't be null");
        return new DB(dbDir);
    }

    private DB(File dbDir) {
        this.dbDir = dbDir;
    }

    public File getDbDir() {
        return dbDir;
    }

    public void insert(Object object){

    }

}
