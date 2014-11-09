package gsonDB.document;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import gsonDB.AbstractTest;
import gsonDB.Gender;
import gsonDB.Person;
import junit.framework.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by Sleiman on 25/10/2014.
 */
public class BasicDocumentStoreTest extends AbstractTest {

    @Test
    public void testCanInsertDocuments() {
        List<Person> persons = Person.createShortList();
        BasicDocumentStore<Person> basicDocumentStore = new BasicDocumentStore<>(Person.class, testDB);
        for (Person person : persons) {
            basicDocumentStore.insert(person);
        }
        List<Person> fromDB = basicDocumentStore.findAll();
        Assert.assertEquals(fromDB.size(), persons.size());
    }

    @Test
    public void testCanFindDocument() {
        List<Person> persons = Person.createShortList();
        BasicDocumentStore<Person> basicDocumentStore = new BasicDocumentStore<>(Person.class, testDB);
        for (Person person : persons) {
            basicDocumentStore.insert(person);
        }
        List<Person> females = basicDocumentStore.find(new Predicate<Person>() {
            @Override
            public boolean apply(Person input) {
                return input.getGender() == Gender.FEMALE;
            }
        });
        for (Person female : females) {
            Assert.assertTrue(female.getGender() == Gender.FEMALE);
        }
    }

    @Test
    public void testCanFindDocumentById() {
        Person person = Person.createShortList().get(0);
        BasicDocumentStore<Person> basicDocumentStore = new BasicDocumentStore<>(Person.class, testDB);
        String id = basicDocumentStore.insert(person);
        Optional<Person> resultOptional = basicDocumentStore.findById(id);

        Assert.assertTrue(resultOptional.isPresent());
        Assert.assertEquals(person.getEmail(), resultOptional.get().getEmail());
    }

    @Test
    public void testCannotFindAbsentDocument() {
        Person person = Person.createShortList().get(0);
        BasicDocumentStore<Person> basicDocumentStore = new BasicDocumentStore<>(Person.class, testDB);
        basicDocumentStore.insert(person);

        Optional<Person> resultOptional = basicDocumentStore.findById(UUID.randomUUID().toString());
        Assert.assertFalse(resultOptional.isPresent());
    }

    @Test
    public void testCanDeleteDocument() {

        Person person = Person.createShortList().get(0);
        BasicDocumentStore<Person> basicDocumentStore = new BasicDocumentStore<>(Person.class, testDB);

        String objectId = basicDocumentStore.insert(person);
        Assert.assertEquals(1, basicDocumentStore.count());

        basicDocumentStore.delete(objectId);
        Assert.assertEquals(0, basicDocumentStore.count());

    }

    @Test
    public void testCanUpdateDocument() {
        Person person = Person.createShortList().get(0);

        BasicDocumentStore<Person> basicDocumentStore = new BasicDocumentStore<>(Person.class, testDB);
        String objectId = basicDocumentStore.insert(person);

        Person newPerson = Person.createShortList().get(1);

        boolean update = basicDocumentStore.update(objectId, newPerson);
        Assert.assertTrue(update);
    }

    @Test
    public void testFilterWithSorting() {
        List<Person> persons = Person.createShortList();

        BasicDocumentStore<Person> basicDocumentStore = new BasicDocumentStore<>(Person.class, testDB);
        for (Person person : persons) {
            basicDocumentStore.insert(person);
        }
        Comparator<Person> comparator = new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return o1.getFirstName().compareTo(o2.getFirstName());
            }
        };
        Collections.sort(persons,comparator);

        List<Person> fromDB = basicDocumentStore.findAll(comparator);


        for (int i = 0; i < persons.size(); i++) {
            Assert.assertTrue(persons.get(i).equals(fromDB.get(i)));
        }


    }

}

