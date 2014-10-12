package gsonDB.document;

import gsonDB.AbstractTest;
import gsonDB.Foo;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Sleiman on 08/10/2014.
 */
public class DeleteOperationsTest extends AbstractTest {

    @Test
    public void testCanDeleteRecordFromTheMiddle() throws IOException {
        DocumentProcessor fooDocumentProcessor = DocumentProcessor.getDocumentProcessor(Foo.class, testDB);

        Foo foo1 = new Foo(1, "foo1");
        fooDocumentProcessor.insert(foo1);

        Foo foo2 = new Foo(2, "foo2");
        fooDocumentProcessor.insert(foo2);

        Foo foo3 = new Foo(3, "foo3");
        fooDocumentProcessor.insert(foo3);

        Assert.assertTrue(fooDocumentProcessor.findAll(Foo.class).size() == 3);

        fooDocumentProcessor.delete("2", Foo.class);

        Assert.assertTrue(fooDocumentProcessor.findAll(Foo.class).size() == 2);

    }

    @Test
    public void testCanDeleteRecordFromTheEnd() throws IOException {

        DocumentProcessor fooDocumentProcessor = DocumentProcessor.getDocumentProcessor(Foo.class, testDB);

        Foo foo1 = new Foo(1, "foo1");
        fooDocumentProcessor.insert(foo1);

        Foo foo2 = new Foo(2, "foo2");
        fooDocumentProcessor.insert(foo2);

        Foo foo3 = new Foo(3, "foo3");
        fooDocumentProcessor.insert(foo3);

        Assert.assertTrue(fooDocumentProcessor.findAll(Foo.class).size() == 3);

        fooDocumentProcessor.delete("3", Foo.class);

        Assert.assertTrue(fooDocumentProcessor.findAll(Foo.class).size() == 2);

    }


}
