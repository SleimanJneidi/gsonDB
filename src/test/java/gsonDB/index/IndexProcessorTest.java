package gsonDB.index;

import gsonDB.AbstractTest;
import gsonDB.Person;
import gsonDB.document.DocumentProcessor;
import gsonDB.Foo;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.util.List;

/**
 * Created by Sleiman on 30/09/2014.
 */
public class IndexProcessorTest extends AbstractTest{


    @Test
    public void testCanAddTypeToIndex() throws IOException {
        IndexProcessor indexProcessor = IndexProcessor.getIndexHandler(Person.class, testDB);
        Assert.assertEquals(Person.class, indexProcessor.getEntityType());
        Assert.assertTrue(indexProcessor instanceof DefaultIndexProcessor);
    }


}

