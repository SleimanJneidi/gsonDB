package gsonDB;

import gsonDB.utils.FileUtils;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * Created by Sleiman on 12/10/2014.
 */
public class FileUtilsTest extends AbstractTest {

    @Test
    public void testCanPushBuffer() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(new File(testDB.getDBDir() + "/file.dat"),"rw");

        randomAccessFile.write("1".getBytes());
        randomAccessFile.write("2".getBytes());
        randomAccessFile.write("3".getBytes());

        ByteBuffer byteBuffer = ByteBuffer.wrap("0".getBytes());
        FileUtils.pushBuffer(randomAccessFile,byteBuffer,0);
        byte[]buffer = new byte[(int)randomAccessFile.length()];
        randomAccessFile.seek(0);
        randomAccessFile.readFully(buffer);
        String string = new String(buffer);

        Assert.assertEquals("0123",string);
    }
}
