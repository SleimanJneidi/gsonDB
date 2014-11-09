package gsonDB.document;

import com.google.common.base.Preconditions;
import gsonDB.DB;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * Created by Sleiman on 08/11/2014.
 */
public class CappedDocumentStore<T> extends TypedDocumentStore<T> {

    private final int cap;

    public CappedDocumentStore(Class<T> entity, DB db, int cap) {
        super(entity, db);
        Preconditions.checkArgument(cap > 0);
        this.cap = cap;
    }
    @Override
    public String insert(T object){
        if(this.count() == cap){
            File[] files = this.documentDir.listFiles();
            assert files != null;
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    try {
                        BasicFileAttributes file1Attributes = Files.readAttributes(file1.toPath(), BasicFileAttributes.class);
                        BasicFileAttributes file2Attributes = Files.readAttributes(file2.toPath(), BasicFileAttributes.class);
                        return file1Attributes.creationTime().compareTo(file2Attributes.creationTime());

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            if(files.length>0){
                files[files.length -1].delete();
            }

        }
        return super.insert(object);
     }
}
