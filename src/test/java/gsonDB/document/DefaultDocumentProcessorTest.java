package gsonDB.document;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.gson.JsonObject;
import gsonDB.AbstractTest;
import gsonDB.Foo;
import gsonDB.Gender;
import gsonDB.Person;
import gsonDB.index.IndexProcessor;
import junit.framework.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by Sleiman on 04/10/2014.
 */
public class DefaultDocumentProcessorTest extends AbstractTest {

    private DocumentProcessor personDocumentProcessor() throws FileNotFoundException {
        return DocumentProcessor.getDocumentProcessor(Person.class, testDB);
    }


    @Test
    public void testCanInsertDocuments() throws IOException {

        final List<Person> shortList = Person.createShortList();
        for (Person person : shortList) {
            personDocumentProcessor().insert(person);
        }

        List<Person> fromDB = personDocumentProcessor().findAll(Person.class);
        Assert.assertTrue(fromDB.size() == shortList.size());
    }

    @Test
    public void testCanGenerateKey() throws IOException {
        Person person = Person.createShortList().get(0);
        JsonObject jsonObject = personDocumentProcessor().insert(person);
        String generatedKeyString = jsonObject.get("id").getAsString();
        Long generatedKey = Long.parseLong(generatedKeyString);
        Assert.assertNotNull(generatedKey);
    }

    @Test
    public void testCanUseProvidedKey() throws IOException {
        Foo foo = new Foo(43, "Dave");
        JsonObject jsonObject = personDocumentProcessor().insert(foo);
        Assert.assertEquals(jsonObject.get("id").getAsInt(), foo.getId());
    }

    @Test
    public void testCanFetchKeyByPrimaryKey() throws IOException {
        Foo foo = new Foo(43, "Dave");
        personDocumentProcessor().insert(foo);

        Foo foo1 = personDocumentProcessor().find(String.valueOf(foo.getId()), Foo.class);

        Assert.assertEquals(foo.getId(), foo1.getId());
        Assert.assertEquals(foo.getName(), foo1.getName());
    }

    @Test
    public void testCanQueryByPredicates() throws IOException {

        final List<Person> shortList = Person.createShortList();
        for (Person person : shortList) {
            personDocumentProcessor().insert(person);
        }

        final List<Person> personsFromDB = personDocumentProcessor().find(Person.class, new Predicate<Person>() {
            @Override
            public boolean apply(Person input) {
                return input.getFirstName().startsWith("J");
            }
        });

        for (Person person : personsFromDB) {
            Assert.assertTrue(person.getFirstName().startsWith("J"));
        }

    }

    @Test
    public void testCanQueryCompositePredicate() throws IOException {
        final List<Person> shortList = Person.createShortList();
        for (Person person : shortList) {
            personDocumentProcessor().insert(person);
        }

        Predicate<Person> startsWithJ = new Predicate<Person>() {
            @Override
            public boolean apply(Person input) {
                return input.getFirstName().startsWith("J");
            }
        };
        Predicate<Person> isMale = new Predicate<Person>() {
            @Override
            public boolean apply(Person input) {
                return input.getGender() == Gender.MALE;
            }
        };

        final List<Person> personsFromDB = personDocumentProcessor().find(Person.class, Predicates.and(startsWithJ, isMale));

        for (Person person : personsFromDB) {
            Assert.assertTrue(person.getFirstName().startsWith("J") && person.getGender() == Gender.MALE);
        }
    }


}

