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
    public void testCanInsertIntoSortedIndex() throws IOException {


        SortedIndexProcessor indexProcessor = new SortedIndexProcessor(Foo.class, testDB);
        final List<IndexKeyEntry> indexKeyEntries = mockedIndexEntries();

        for (IndexKeyEntry indexKeyEntry : indexKeyEntries) {
            indexProcessor.insertNewIndexEntry(indexKeyEntry);
        }
        IndexKeyEntry indexKeyEntry3 = new IndexKeyEntry("3", 74, 180);
        indexProcessor.insertNewIndexEntry(indexKeyEntry3);

        IndexKeyEntry indexKeyEntry0 = new IndexKeyEntry("0", 74, 180);
        indexProcessor.insertNewIndexEntry(indexKeyEntry0);

        final Iterable<IndexKeyEntry> indexKeyEntryIterable = indexProcessor.indexKeyEntryIterable();

        final String[] strings = Iterables.toArray(Iterables.transform(indexKeyEntryIterable, new Function<IndexKeyEntry, String>() {
            @Override
            public String apply(IndexKeyEntry input) {
                return input.getKey();
            }
        }), String.class);

        org.junit.Assert.assertArrayEquals(new String[]{"0", "1", "2", "3", "5"}, strings);


    }

    @Test
    public void testCanFindElement() throws IOException {

        SortedIndexProcessor indexProcessor = new SortedIndexProcessor(Foo.class, testDB);
        final List<IndexKeyEntry> indexKeyEntries = mockedIndexEntries();

        for (IndexKeyEntry indexKeyEntry : indexKeyEntries) {
            indexProcessor.insertNewIndexEntry(indexKeyEntry);
        }
        IndexKeyEntry indexKeyEntry3 = new IndexKeyEntry("3", 74, 180);
        indexProcessor.insertNewIndexEntry(indexKeyEntry3);

        IndexKeyEntry indexKeyEntry0 = new IndexKeyEntry("0", 74, 180);
        indexProcessor.insertNewIndexEntry(indexKeyEntry0);

        Assert.assertEquals(indexKeyEntry0, indexProcessor.getIndexByKey("0").get());
        Assert.assertEquals(indexKeyEntry3, indexProcessor.getIndexByKey("3").get());

        Assert.assertFalse(indexProcessor.getIndexByKey("4").isPresent());

    }

    private List<IndexKeyEntry> mockedIndexEntries() {
        IndexKeyEntry indexKeyEntry = new IndexKeyEntry("1", 10, 15);
        IndexKeyEntry indexKeyEntry1 = new IndexKeyEntry("2", 1, 150);
        IndexKeyEntry indexKeyEntry2 = new IndexKeyEntry("5", 1, 11);

        return Arrays.asList(indexKeyEntry, indexKeyEntry1, indexKeyEntry2);

    }


}
