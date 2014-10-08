package gsonDB.utils;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Sleiman on 07/10/2014.
 */
public class FileUtils {

    public static void deleteBytes(RandomAccessFile file, long from, int length) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.length() >= (from + length));

        file.seek(from + length);
        FileChannel fileChannel = file.getChannel();
        fileChannel.position(from + length);

        ByteBuffer bytesToCopy = ByteBuffer.allocate((int) (file.length() - (from + length)));

        fileChannel.read(bytesToCopy);
        fileChannel.truncate(from);
        bytesToCopy.flip();

        fileChannel.write(bytesToCopy);
    }
}
