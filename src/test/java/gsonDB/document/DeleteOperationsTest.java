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
    public void testCanDeleteRecord() throws IOException {

        DocumentProcessor fooDocumentProcessor = DocumentProcessor.getDocumentProcessor(Foo.class, testDB);

        Foo foo1 = new Foo(1, "foo1");
        fooDocumentProcessor.insert(foo1);

        Foo foo2 = new Foo(2, "foo2");
        fooDocumentProcessor.insert(foo2);

        Foo foo3 = new Foo(3, "foo3");
        long id =  fooDocumentProcessor.insert(foo3).get(BasicDocumentProcessor.DEFAULT_ID_NAME).getAsLong();

        Assert.assertTrue(fooDocumentProcessor.findAll(Foo.class).size() == 3);

        fooDocumentProcessor.delete(id, Foo.class);

        Assert.assertTrue(fooDocumentProcessor.findAll(Foo.class).size() == 2);

    }


}
