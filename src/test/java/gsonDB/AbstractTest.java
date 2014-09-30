package gsonDB;

import org.junit.rules.TemporaryFolder;

/**
 * Created by Sleiman on 30/09/2014.
 */
public abstract class AbstractTest {

    public final TemporaryFolder folder  = new TemporaryFolder();
    public final DB testDB = DB.getDB(folder.newFolder("TestDBFiles"));;

}
