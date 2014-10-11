package gsonDB;

import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * Created by Sleiman on 30/09/2014.
 */
public abstract class AbstractTest {

    @Rule
    public static final TemporaryFolder folder = new TemporaryFolder();

    public static final DB testDB = GsonDB.getDB(folder.newFolder("TestDBFiles"));

    @After
    public void cleanup() {
        File dbDir = testDB.getDBDir();
        for (File file : dbDir.listFiles()) {
            file.delete();
        }

    }
}
