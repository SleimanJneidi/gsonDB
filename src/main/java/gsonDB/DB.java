package gsonDB;

import java.io.File;

/**
 * Created by Sleiman on 11/10/2014.
 */
public interface DB {
    File getDBDir();
    long size();
}
