package gsonDB.document;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import gsonDB.AbstractTest;
import gsonDB.Person;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

/**
 *
 * Created by Sleiman on 09/11/2014.
 */

public class CappedDocumentStoreTest extends AbstractTest {

    @Test
    public void testCanInsertDocuments(){
        Person.createShortList();
        List<Person> persons = Person.createShortList();
        int cap = 3;
        CappedDocumentStore<Person> cappedDocumentStore = new CappedDocumentStore<>(Person.class, testDB, cap );
        for (Person person : persons) {
            cappedDocumentStore.insert(person);
        }

        Assert.assertEquals(cap,cappedDocumentStore.count());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateCap(){
        new CappedDocumentStore<>(Person.class, testDB, 0 );
    }


}