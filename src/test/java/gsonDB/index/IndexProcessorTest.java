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
public class IndexProcessorTest extends AbstractTest{

    @Before
    public void setup(){

    }

    @Test
    public void testCanAddTypeToIndex() throws IOException {
        IndexProcessor indexProcessor = IndexProcessor.getIndexHandler(Person.class, testDB);
        Assert.assertEquals(Person.class, indexProcessor.getEntityType());
        Assert.assertTrue(IndexProcessor.allIndexHandlers().size()==1);
        Assert.assertTrue(indexProcessor instanceof DefaultIndexProcessor);
    }

}

