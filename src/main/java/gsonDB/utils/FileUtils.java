package gsonDB.utils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Sleiman on 07/10/2014.
 */
public class FileUtils {

    /**
     * Deletes chunk of data from a file, it copies all the records that reside below length
     * to a temporary buffer and write them again at the tail
     *
     * @param file
     * @param offset
     * @param length
     * @throws IOException
     */
    public static void deleteBytes(RandomAccessFile file, long offset, int length) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.length() >= (offset + length));

        FileChannel fileChannel = file.getChannel();
        fileChannel.position(offset + length);

        ByteBuffer bytesToCopy = ByteBuffer.allocate((int) (file.length() - (offset + length)));

        fileChannel.read(bytesToCopy);
        fileChannel.truncate(offset);
        bytesToCopy.flip();

        fileChannel.write(bytesToCopy);
    }

    public static void pushBuffer(RandomAccessFile file, ByteBuffer byteBuffer, long position) throws IOException {
        FileChannel fileChannel = file.getChannel();
        int lengthToCopy = (int) (file.length() - position);

        ByteBuffer bytesToCopy = ByteBuffer.allocate(lengthToCopy);
        fileChannel.position(position);
        fileChannel.read(bytesToCopy);
        fileChannel.position(position);

        bytesToCopy.flip();

        fileChannel.write(byteBuffer);
        fileChannel.write(bytesToCopy);

    }

    public static ByteBuffer join(ByteBuffer... byteBuffers) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (ByteBuffer byteBuffer : byteBuffers) {
            outputStream.write(byteBuffer.array());
        }
        ByteBuffer joinedBuffer = ByteBuffer.wrap(outputStream.toByteArray());
        return joinedBuffer;
    }

    public static long directorySize(final File directory) {
        Preconditions.checkNotNull(directory);
        Function<File, Long> dirSizeFunc = new Function<File, Long>() {
            private long size = 0L;

            @Override
            public Long apply(File dir) {
                for (File file : dir.listFiles()) {
                    if (file.isDirectory()) {
                        return apply(file);
                    } else {
                        size += file.length();
                    }

                }

                return size;
            }
        };

        long size = dirSizeFunc.apply(directory);
        return size;
    }
}
