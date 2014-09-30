package gsonDB.index;

import gsonDB.DB;
import gsonDB.Person;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Sleiman on 30/09/2014.
 */
public class IndexHandlerTest {
    public TemporaryFolder folder;
    private DB db;

    @Before
    public void setup(){
        this.folder = new TemporaryFolder();
        this.db = DB.getDB(folder.newFolder("TestDBFiles"));
    }

    @Test
    public void testCanAddTypeToIndex() throws IOException {
        IndexHandler indexHandler = IndexHandler.getIndexHandler(Person.class,db);
        Assert.assertEquals(Person.class,indexHandler.getEntityType());
        Assert.assertTrue(IndexHandler.allIndexHandlers().size()==1);
        Assert.assertTrue(indexHandler.getNumberOfRecords()==0);
    }

}
