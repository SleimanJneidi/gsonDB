package gsonDB;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;


/**
 * Created by Sleiman on 28/09/2014.
 */
public class DBTest{

    public TemporaryFolder folder;

    @Before
    public void setup(){
        this.folder = new TemporaryFolder();
    }

    @Test
    public void testCreateDB() throws IOException {
        DB db = DB.getDB(folder.newFolder("TestDBFiles"));
        Assert.assertNotNull(db);
        Assert.assertNotNull(db.getDbDir());
    }


}
