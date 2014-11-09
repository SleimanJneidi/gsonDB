package gsonDB.document;

import com.google.common.base.Function;
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
 *
 * Created by Sleiman on 25/10/2014.
 */
public class TypedDocumentStore<T> extends AbstractDocumentStore<T> {

    private final Type type;
    private final Gson gson = new Gson();

    public TypedDocumentStore(Class<T> entity, DB db) {
        super(entity.getSimpleName(), db);
        type = TypeToken.of(entity).getType();
    }


    @Override
    protected Function<T, byte[]> encode() {
        return new Function<T, byte[]>() {
            @Override
            public byte[] apply(T object) {
                return gson.toJson(object).getBytes();
            }
        };
    }

    @Override
    protected Function<byte[], T> decode() {
        return new Function<byte[], T>() {
            @Override
            public T apply(byte[] buffer) {
                String jsonString = new String(buffer);
                return gson.fromJson(jsonString, type);
            }
        };
    }

    @Override
    protected  Function<T,String> idGenerator(){

        return new Function<T, String>() {
            @Override
            public String apply(T input) {
                return UUID.randomUUID().toString();
            }
        };
    }

}
