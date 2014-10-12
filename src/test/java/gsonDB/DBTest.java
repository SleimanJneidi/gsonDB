package gsonDB;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import gsonDB.index.fts.GsonInvertedIndexStore;
import gsonDB.index.fts.IndexTuple;
import gsonDB.index.fts.InvertedIndexStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


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
