package gsonDB.document;

import gsonDB.AbstractTest;
import gsonDB.Person;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by Sleiman on 09/11/2014.
 */
public class CappedDocumentStoreTest extends AbstractTest {

    @Test
    public void testCanInsertDocuments(){
        Person.createShortList();
        List<Person> persons = Person.createShortList();
        CappedDocumentStore<Person> cappedDocumentStore = new CappedDocumentStore<>(Person.class, testDB,3);
        for (Person person : persons) {
            cappedDocumentStore.insert(person);
        }

        Assert.assertEquals(3,cappedDocumentStore.count());
    }
}
