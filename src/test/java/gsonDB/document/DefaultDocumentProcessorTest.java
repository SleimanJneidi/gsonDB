package gsonDB.document;

import gsonDB.AbstractTest;
import gsonDB.Person;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Sleiman on 04/10/2014.
 */
public class DefaultDocumentProcessorTest extends AbstractTest {

    @Test
    public void testCanInsertDocuments() throws IOException {


        DocumentProcessor documentProcessor = DocumentProcessor.getDocumentProcessor(Person.class, testDB);

        final List<Person> shortList = Person.createShortList();
        for (Person person : shortList) {
            documentProcessor.insert(person);
        }

        List<Person> fromDB = documentProcessor.findAll(Person.class);
        Assert.assertTrue(fromDB.size() == shortList.size());
    }
}
