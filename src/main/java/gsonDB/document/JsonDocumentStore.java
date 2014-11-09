package gsonDB.document;

import com.google.common.base.Function;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gsonDB.DB;

import java.util.UUID;

/**
 *
 * Created by Sleiman on 09/11/2014.
 */
public class JsonDocumentStore extends AbstractDocumentStore<JsonObject> {

    private final JsonParser jsonParser = new JsonParser();

    public JsonDocumentStore(String collectionName, DB db) {
        super(collectionName, db);
    }

    @Override
    protected Function<JsonObject, byte[]> encode() {
        return new Function<JsonObject, byte[]>() {
            @Override
            public byte[] apply(JsonObject input) {
                return input.toString().getBytes();
            }
        };
    }

    @Override
    protected Function<byte[], JsonObject> decode() {
        return new Function<byte[], JsonObject>() {
            @Override
            public JsonObject apply(byte[] input) {
                String jsonString = new String(input);
                return jsonParser.parse(jsonString).getAsJsonObject();
            }
        };
    }

    @Override
    protected Function<JsonObject, String> idGenerator() {
        return new Function<JsonObject, String>() {
            @Override
            public String apply(JsonObject input) {
                return UUID.randomUUID().toString();
            }
        };
    }
}
