package gsonDB;

import java.io.File;

/**
 * Created by Sleiman on 28/09/2014.
 */
public class DB {
    private final File dbDir;

    public static DB getDB(final File dbDir) {
        return new DB(dbDir);
    }

    private DB(File dbDir) {
        this.dbDir = dbDir;
    }

    public File getDbDir() {
        return dbDir;
    }
}
