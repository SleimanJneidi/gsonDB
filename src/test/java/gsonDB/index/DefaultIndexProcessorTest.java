package gsonDB.index;

import com.google.common.base.Optional;
import gsonDB.AbstractTest;
import gsonDB.LongKeyException;
import gsonDB.Person;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Sleiman on 03/10/2014.
 */
public class DefaultIndexProcessorTest extends AbstractTest {

    @Test
    public void testCanInsertIndexKeyEntry() throws Exception {
        try (DefaultIndexProcessor processor = new DefaultIndexProcessor(Person.class, this.testDB)) {
            List<IndexKeyEntry> mockedIndexEntries = mockedIndexEntries();

            for (IndexKeyEntry indexKeyEntry : mockedIndexEntries()) {
                processor.insertNewIndexEntry(indexKeyEntry);
            }

            Assert.assertEquals(mockedIndexEntries.size(), processor.count());

        }

    }

    @Test(expected = LongKeyException.class)
    public void testShouldThrowLongKeyException() {
        new IndexKeyEntry(UUID.randomUUID().toString() + "Add Something Extra", 70, 18);
    }

    @Test
    public void testCanUpdateIndex() throws Exception {

        DefaultIndexProcessor processor = new DefaultIndexProcessor(Person.class, this.testDB);
        List<IndexKeyEntry> mockedIndexEntries = mockedIndexEntries();

        for (IndexKeyEntry indexKeyEntry : mockedIndexEntries) {
            processor.insertNewIndexEntry(indexKeyEntry);
        }

        Optional<IndexKeyEntry> currentIndexByKeyOptional = processor.getIndexByKey("someId");
        Assert.assertTrue(currentIndexByKeyOptional.isPresent());

        IndexKeyEntry currentIndexByKey = currentIndexByKeyOptional.get();
        Assert.assertNotNull(currentIndexByKey);

        IndexKeyEntry newIndexKeyEntry = new IndexKeyEntry("someId", 90, 95);
        processor.updateIndexKeyEntry(newIndexKeyEntry);

        currentIndexByKey = processor.getIndexByKey("someId").get();
        Assert.assertEquals(90,currentIndexByKey.getDataFilePointer());
        Assert.assertEquals(95,currentIndexByKey.getRecordSize());

    }

    private List<IndexKeyEntry> mockedIndexEntries() {
        IndexKeyEntry indexKeyEntry = new IndexKeyEntry("1234", 10, 15);
        IndexKeyEntry indexKeyEntry1 = new IndexKeyEntry("hi", 1, 150);
        IndexKeyEntry indexKeyEntry2 = new IndexKeyEntry(UUID.randomUUID().toString(), 70, 18);
        IndexKeyEntry indexKeyEntry3 = new IndexKeyEntry("xx--ff", 70, 24);
        IndexKeyEntry indexKeyEntry4 = new IndexKeyEntry("someId", 1, 11);
        return Arrays.asList(indexKeyEntry, indexKeyEntry1, indexKeyEntry2, indexKeyEntry3, indexKeyEntry4);

    }

}
