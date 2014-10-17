package gsonDB;

import com.google.common.base.Predicate;

import java.io.File;
import java.util.List;

/**
 * Created by Sleiman on 11/10/2014.
 */
public interface DB {

    File getDBDir();

    <T> List<T> findAll(Class<T> clazz);

    <T> List<T> find(Class<T> clazz, Predicate<T> filter);

    long size();
}
