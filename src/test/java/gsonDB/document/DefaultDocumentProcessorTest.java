package gsonDB.document;

import com.google.gson.JsonObject;
import gsonDB.AbstractTest;
import gsonDB.Person;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Created by Sleiman on 04/10/2014.
 */
public class DefaultDocumentProcessorTest extends AbstractTest {

    private static DocumentProcessor documentProcessor;

    static {
        try {
            documentProcessor = DocumentProcessor.getDocumentProcessor(Person.class, testDB);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCanInsertDocuments() throws IOException {

        final List<Person> shortList = Person.createShortList();
        for (Person person : shortList) {
            documentProcessor.insert(person);
        }

        List<Person> fromDB = documentProcessor.findAll(Person.class);
        Assert.assertTrue(fromDB.size() == shortList.size());
    }

    @Test
    public void testCanGenerateKey() throws IOException {
        Person person = Person.createShortList().get(0);
        JsonObject jsonObject =  documentProcessor.insert(person);
        String generatedKeyString = jsonObject.get("id").getAsString();
        UUID generatedKey =  UUID.fromString(generatedKeyString);
        Assert.assertNotNull(generatedKey);
    }

    @Test
    public void testCanUseProvidedKey() throws IOException{
        Foo foo = new Foo(43,"Dave");
        JsonObject jsonObject =  documentProcessor.insert(foo);
        Assert.assertEquals(jsonObject.get("id").getAsInt(),foo.getId());
    }

    @Test
    public void testCanFetchKeyByPrimaryKey() throws IOException{
        Foo foo = new Foo(43,"Dave");
        documentProcessor.insert(foo);

        Foo foo1 = documentProcessor.find(Foo.class,String.valueOf(foo.getId()));

        Assert.assertEquals(foo.getId(),foo1.getId());
        Assert.assertEquals(foo.getName(),foo1.getName());
    }
}

class Foo{
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
