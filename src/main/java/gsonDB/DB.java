package gsonDB;

import java.io.File;
import java.util.Set;

/**
 *
 * Created by Sleiman on 11/10/2014.
 */
public interface DB {

    File getDBDir();

    Set<String> collections();

    long size();
}
