package gsonDB.document;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import gsonDB.DB;
import gsonDB.utils.CompressionUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by Sleiman on 25/10/2014.
 */
public class BasicDocumentStore<T> implements DocumentStore<T>{

    protected final File documentDir;
    private final Type type;
    private final Gson gson = new Gson();

    public BasicDocumentStore(Class<T> entity, DB db) {

        type = TypeToken.of(entity).getType();
        String dirName = entity.getSimpleName() + "_data";
        this.documentDir = new File(db.getDBDir(), dirName);
        if(!this.documentDir.exists()){
            this.documentDir.mkdir();
        }

    }

    @Override
    public String insert(T object) {
        String id = UUID.randomUUID().toString();
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
        if(!fileOptional.isPresent()){
            return false;
        }
        File documentFile = fileOptional.get();
        return documentFile.delete();
    }

    @Override
    public boolean update(String id, T object) {
        Optional<File> fileOptional = fileWithName(id);
        if(!fileOptional.isPresent()){
            return false;
        }
        try(OutputStream outputStream = new FileOutputStream(fileOptional.get())){
            writeObject(outputStream,object);
            return true;
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<T> findById(final String id) {
        Preconditions.checkNotNull(id);
        Optional<File> fileOptional = fileWithName(id);

        if(!fileOptional.isPresent()){
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
    public List<T> findAll(Comparator<? super T> comparator){
        return find(new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return true;
            }
        },comparator);
    }

    @Override
    public List<T> find(Predicate<T> predicate) {
        List<T> results = new ArrayList<>();

        for (File documentFile : this.documentDir.listFiles()) {
            T object = readFromFile(documentFile);
            if(predicate.apply(object)){
                results.add(object);
            }
        }

        return results;
    }

    @Override
    public List<T> find(Predicate<T> filter, Comparator<? super T> comparator){
        Preconditions.checkNotNull(comparator);
        Preconditions.checkNotNull(filter);
        Set<T> results = new TreeSet<>(comparator);
        for (File documentFile : this.documentDir.listFiles()) {
            T object = readFromFile(documentFile);
            if(filter.apply(object)){
                results.add(object);
            }
        }
        return new ArrayList<>(results);
    }

    @Override
    public int count() {
        return this.documentDir.list().length;
    }

    private T readFromFile(File documentFile) {
        byte[] compressedBuffer = new byte[(int) documentFile.length()];
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(documentFile))) {
            reader.read(compressedBuffer);
            byte[] buffer = CompressionUtils.decompress(compressedBuffer);
            String jsonString = new String(buffer);
            T json = this.gson.fromJson(jsonString, type);

            return json;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<File> fileWithName(final String fileName){
        File[] matchingFiles = documentDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return fileName.equals(name);
            }
        });
        if(matchingFiles == null || matchingFiles.length ==0){
            return Optional.absent();
        }
        return Optional.of(matchingFiles[0]);
    }

    private void writeObject(final OutputStream outputStream, T object){
        byte[] jsonStringBytes = gson.toJson(object).getBytes();
        byte[] compressedBytes = CompressionUtils.compress(jsonStringBytes);
        try {
            outputStream.write(compressedBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
