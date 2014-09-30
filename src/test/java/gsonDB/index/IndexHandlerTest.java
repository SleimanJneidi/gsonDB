package gsonDB.index;

import gsonDB.AbstractTest;
import gsonDB.Person;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Sleiman on 30/09/2014.
 */
public class IndexHandlerTest extends AbstractTest{

    @Before
    public void setup(){

    }

    @Test
    public void testCanAddTypeToIndex() throws IOException {
        IndexHandler indexHandler = IndexHandler.getIndexHandler(Person.class, testDB);
        Assert.assertEquals(Person.class, indexHandler.getEntityType());
        Assert.assertTrue(IndexHandler.allIndexHandlers().size()==1);
        Assert.assertTrue(indexHandler.getNumberOfRecords()==0);

    }

}
