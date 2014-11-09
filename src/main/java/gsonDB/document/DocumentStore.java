package gsonDB.document;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.Comparator;
import java.util.List;

/**
 * Created by Sleiman on 08/11/2014.
 */
public interface DocumentStore<T> {

    public String insert(T object);

    public boolean delete(String id);

    public boolean update(String id, T object);

    public Optional<T> findById(String id);

    public List<T> find(Predicate<T> filter);

    public List<T> find(Predicate<T> filter, Comparator<? super T> comparator);

    public List<T> findAll();

    public List<T> findAll(Comparator<? super T> comparator);

    public int count();

}
