package gsonDB.utils;

/**
 * Created by Sleiman on 18/10/2014.
 */
public abstract class ExecuteCloseable<T> {

    abstract T execute();

    <K extends AutoCloseable> T around(K closeable) {
        try {
            return execute();
        } finally {
            try {
                closeable.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
