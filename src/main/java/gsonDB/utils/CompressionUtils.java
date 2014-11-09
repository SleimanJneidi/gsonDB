package gsonDB.utils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
/**
 * Created by Sleiman on 08/11/2014.
 */

public class CompressionUtils {
    public static byte[] compress(byte[] uncompressedBuffer) {
        Deflater deflater = new Deflater();
        deflater.setInput(uncompressedBuffer);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(uncompressedBuffer.length);

        try {
            deflater.finish();
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            byte[] output = outputStream.toByteArray();
            return output;

        } finally {
            try {
                deflater.end();
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static byte[] decompress(byte[] compressedBuffer) {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedBuffer);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedBuffer.length);
        try {
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            byte[] output = outputStream.toByteArray();
            return output;
        } catch (DataFormatException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inflater.end();
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
