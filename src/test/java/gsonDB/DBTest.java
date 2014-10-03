package gsonDB;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;


/**
 * Created by Sleiman on 28/09/2014.
 */
public class DBTest extends AbstractTest{

    @Before
    public void setup(){
    }

    @Test
    public void testCreateDB() throws IOException {
        Assert.assertNotNull(testDB);
        Assert.assertNotNull(testDB.getDbDir());
    }



}
