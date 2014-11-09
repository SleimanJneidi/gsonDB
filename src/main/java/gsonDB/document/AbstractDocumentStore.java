package gsonDB.document;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import gsonDB.DB;
import gsonDB.utils.CompressionUtils;

import java.io.*;
import java.util.*;

/**
 *
 * Created by Sleiman on 09/11/2014.
 *
 */
public abstract class AbstractDocumentStore<T> implements DocumentStore<T> {

    protected final File documentDir;

    protected AbstractDocumentStore(String collectionName, DB db){
        String dirName = collectionName + "_data";
        this.documentDir = new File(db.getDBDir(), dirName);
        if(!this.documentDir.exists()){
            this.documentDir.mkdir();
        }
    }

    @Override
    public String insert(T object) {
        String id = idGenerator().apply(object);
        File newEntryFileName = new File(documentDir,id);
        try(OutputStream outputStream = new FileOutputStream(newEntryFileName)){
            writeObject(outputStream,object);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        return id;
    }

    @Override
    public boolean delete(String id) {
        Optional<File> fileOptional = fileWithName(id);
        if (!fileOptional.isPresent()) {
            return false;
        }
        File documentFile = fileOptional.get();
        return documentFile.delete();
    }

    @Override
    public boolean update(String id, T object) {
        Optional<File> fileOptional = fileWithName(id);
        if (!fileOptional.isPresent()) {
            return false;
        }
        try (OutputStream outputStream = new FileOutputStream(fileOptional.get())) {
            writeObject(outputStream, object);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<T> findById(final String id) {
        Preconditions.checkNotNull(id);
        Optional<File> fileOptional = fileWithName(id);

        if (!fileOptional.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(readFromFile(fileOptional.get()));
    }

    @Override
    public List<T> findAll() {
        return find(new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return true;
            }
        });
    }

    @Override
    public List<T> findAll(Comparator<? super T> comparator) {
        return find(new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return true;
            }
        }, comparator);
    }

    @Override
    public List<T> find(Predicate<T> predicate) {
        List<T> results = new ArrayList<>();

        for (File documentFile : this.documentDir.listFiles()) {
            T object = readFromFile(documentFile);
            if (predicate.apply(object)) {
                results.add(object);
            }
        }

        return results;
    }

    @Override
    public List<T> find(Predicate<T> filter, Comparator<? super T> comparator) {
        Preconditions.checkNotNull(comparator);
        Preconditions.checkNotNull(filter);
        Set<T> results = new TreeSet<>(comparator);
        for (File documentFile : this.documentDir.listFiles()) {
            T object = readFromFile(documentFile);
            if (filter.apply(object)) {
                results.add(object);
            }
        }
        return new ArrayList<>(results);
    }

    private T readFromFile(File documentFile) {
        byte[] compressedBuffer = new byte[(int) documentFile.length()];
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(documentFile))) {
            reader.read(compressedBuffer);
            byte[] buffer = CompressionUtils.decompress(compressedBuffer);
            return this.decode().apply(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeObject(final OutputStream outputStream, T object){
        byte[] bytes = encode().apply(object);
        byte[] compressedBytes = CompressionUtils.compress(bytes);

        try {
            outputStream.write(compressedBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<File> fileWithName(final String fileName) {
        File[] matchingFiles = documentDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return fileName.equals(name);
            }
        });
        if (matchingFiles == null || matchingFiles.length == 0) {
            return Optional.absent();
        }
        return Optional.of(matchingFiles[0]);
    }
    protected abstract Function<T,byte[]> encode();

    protected abstract Function<byte[],T> decode();

    protected abstract Function<T,String> idGenerator();

    @Override
    public int count() {
        return this.documentDir.list().length;
    }

}
