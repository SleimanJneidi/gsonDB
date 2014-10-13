package gsonDB.index;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.ObjectArrays;
import gsonDB.AbstractTest;
import gsonDB.Foo;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Sleiman on 12/10/2014.
 */
public class SortedIndexTest extends AbstractTest {


    @Test
    public void testCanFindElement() throws IOException {

        SortedIndexProcessor indexProcessor = new SortedIndexProcessor(Foo.class, testDB);

        IndexKeyEntry indexKeyEntry = new IndexKeyEntry(1, 10, 15);
        IndexKeyEntry indexKeyEntry1 = new IndexKeyEntry(2, 1, 150);
        IndexKeyEntry indexKeyEntry2 = new IndexKeyEntry(5, 1, 11);


        indexProcessor.insertNewIndexEntry(indexKeyEntry);
        indexProcessor.insertNewIndexEntry(indexKeyEntry1);
        indexProcessor.insertNewIndexEntry(indexKeyEntry2);


        Assert.assertEquals(indexKeyEntry, indexProcessor.getIndexByKey(1).get());
        Assert.assertEquals(indexKeyEntry2, indexProcessor.getIndexByKey(5).get());

        Assert.assertFalse(indexProcessor.getIndexByKey(4).isPresent());

    }



}
