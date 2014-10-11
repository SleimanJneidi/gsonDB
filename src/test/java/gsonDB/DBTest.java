package gsonDB;

import com.google.common.base.Objects;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;


/**
 * Created by Sleiman on 28/09/2014.
 */
public class DBTest extends AbstractTest {

    @Before
    public void setup() {
    }

    @Test
    public void testCreateDB() throws IOException {


        Assert.assertNotNull(testDB);
        Assert.assertNotNull(testDB.getDBDir());

    }


}
