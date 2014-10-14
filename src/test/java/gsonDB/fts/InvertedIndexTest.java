package gsonDB.fts;

import gsonDB.AbstractTest;
import gsonDB.Foo;
import gsonDB.index.fts.InvertedIndex;
import gsonDB.index.fts.TextIndex;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Sleiman on 14/10/2014.
 */
public class InvertedIndexTest extends AbstractTest {

    @Test
    public void testCanIndexARandomString() throws IOException {
        TextIndex textIndex = InvertedIndex.getInstance(Foo.class,testDB);
        textIndex.index(System.currentTimeMillis(),"This is a pointless string");
    }
}
