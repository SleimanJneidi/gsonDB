package gsonDB.fts;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import gsonDB.AbstractTest;
import gsonDB.Foo;
import gsonDB.index.fts.IndexTuple;
import gsonDB.index.fts.InvertedIndex;
import gsonDB.index.fts.TextIndex;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Sleiman on 14/10/2014.
 */
public class InvertedIndexTest extends AbstractTest {

    @Test
    public void testCanIndexARandomString() throws IOException {
        InvertedIndex textIndex = InvertedIndex.getInstance(Foo.class, testDB);
        textIndex.index(System.currentTimeMillis(), "yet another pointless thing");
    }

    @Test
    public void testCanSearchForAString() throws IOException, InterruptedException {

        InvertedIndex textIndex = InvertedIndex.getInstance(Foo.class, testDB);

        long id1= System.currentTimeMillis();
        Thread.sleep(1);

        long id2= System.currentTimeMillis();
        Thread.sleep(1);

        long id3= System.currentTimeMillis();
        Thread.sleep(1);

        long id4= System.currentTimeMillis();

        textIndex.index(id1, "yet another does pointless thing");
        textIndex.index(id2, "something that does not count");
        textIndex.index(id3, "a mind blowing string does make a difference but not hugely");
        textIndex.index(id4, "programmers never die");

        textIndex.search("does not count");

    }


}