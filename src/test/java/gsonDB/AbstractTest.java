package gsonDB;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * Created by Sleiman on 30/09/2014.
 */
public abstract class AbstractTest {

    @Rule
    public static final TemporaryFolder folder = new TemporaryFolder();

    public static final DB testDB = DB.getDB(folder.newFolder("TestDBFiles"));

    @AfterClass
    public static void cleanup() {

        File dbDir = testDB.getDbDir();
        for (File file : dbDir.listFiles()) {
            file.delete();
        }
    }
}
