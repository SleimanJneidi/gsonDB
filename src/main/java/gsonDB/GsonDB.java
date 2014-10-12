package gsonDB;

import com.google.common.base.Preconditions;
import gsonDB.utils.FileUtils;

import java.io.File;

/**
 * Created by Sleiman on 28/09/2014.
 */

public class GsonDB implements DB {

    private final File dbDir;

    public static DB getDB(final File dbDir) {
        Preconditions.checkNotNull(dbDir,"DB directory file object shouldn't be null");
        Preconditions.checkArgument(dbDir.isDirectory(),"DB file should be a valid directory");
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
    public long size() {
        return FileUtils.directorySize(this.dbDir);
    }
}
