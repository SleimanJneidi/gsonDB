package gsonDB.document;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.gson.JsonObject;
import gsonDB.AbstractTest;
import gsonDB.Gender;
import gsonDB.Person;
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
        UUID generatedKey = UUID.fromString(generatedKeyString);
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

        Foo foo1 = personDocumentProcessor().find(Foo.class, String.valueOf(foo.getId()));

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
    public void testCanQueryCompositePredicate() throws IOException{
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

    @Test
    public void testCanDeleteRecordFromTheMiddle()throws IOException{
        DocumentProcessor fooDocumentProcessor = DocumentProcessor.getDocumentProcessor(Foo.class,testDB);

        Foo foo1 = new Foo(1,"foo1");
        fooDocumentProcessor.insert(foo1);

        Foo foo2 = new Foo(2,"foo2");
        fooDocumentProcessor.insert(foo2);

        Foo foo3 = new Foo(3,"foo3");
        fooDocumentProcessor.insert(foo3);

        Assert.assertTrue(fooDocumentProcessor.findAll(Foo.class).size() == 3);

        fooDocumentProcessor.delete(Foo.class, "2");

        Assert.assertTrue(fooDocumentProcessor.findAll(Foo.class).size() == 2);

    }

    @Test
    public void testCanDeleteRecordFromTheEnd()throws IOException{

        DocumentProcessor fooDocumentProcessor = DocumentProcessor.getDocumentProcessor(Foo.class,testDB);

        Foo foo1 = new Foo(1,"foo1");
        fooDocumentProcessor.insert(foo1);

        Foo foo2 = new Foo(2,"foo2");
        fooDocumentProcessor.insert(foo2);

        Foo foo3 = new Foo(3,"foo3");
        fooDocumentProcessor.insert(foo3);

        fooDocumentProcessor.findAll(Foo.class);
        Assert.assertTrue(fooDocumentProcessor.findAll(Foo.class).size() == 3);

        fooDocumentProcessor.delete(Foo.class, "3");

        Assert.assertTrue(fooDocumentProcessor.findAll(Foo.class).size() == 2);

    }



}

class Foo {
    private int id;
    private String name;

    Foo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
