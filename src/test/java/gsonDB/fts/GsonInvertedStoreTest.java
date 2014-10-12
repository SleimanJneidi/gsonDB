package gsonDB.fts;

import com.google.common.collect.Lists;
import gsonDB.AbstractTest;
import gsonDB.index.fts.GsonInvertedIndexStore;
import gsonDB.index.fts.IndexTuple;
import gsonDB.index.fts.InvertedIndexStore;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Sleiman on 13/10/2014.
 */
public class GsonInvertedStoreTest extends AbstractTest {

    @Test
    public void testCanStoreInvertedIndex() throws IOException {

        InvertedIndexStore indexStore = GsonInvertedIndexStore.getInstance(new File(testDB.getDBDir() + "/index"));
        indexStore.store(dummyMap());
        final Map<String, List<IndexTuple>> fetchedMap = indexStore.load();

        Assert.assertEquals(dummyMap().size(),fetchedMap.size());
    }

    private Map<String, List<IndexTuple>> dummyMap() {
        final Map<String, List<IndexTuple>> indexMap = new ConcurrentHashMap<>();

        indexMap.put("hi", Lists.newArrayList(new IndexTuple("1", 0), new IndexTuple("1", 0), new IndexTuple("1", 0)));
        indexMap.put("yes", Lists.newArrayList(new IndexTuple("1", 0), new IndexTuple("1", 0), new IndexTuple("1", 0)));
        indexMap.put("apple", Lists.newArrayList(new IndexTuple("1", 0), new IndexTuple("1", 0), new IndexTuple("1", 0)));
        return indexMap;
    }
}
