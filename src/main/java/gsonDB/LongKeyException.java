package gsonDB;

import gsonDB.index.DefaultIndexProcessor;
import gsonDB.index.IndexProcessor;

/**
 * Created by Sleiman on 02/10/2014.
 */
public class LongKeyException extends RuntimeException {

    public LongKeyException(){
        super(String.format("Key exceeds the maximum key length: %d bytes", DefaultIndexProcessor.KEY_SIZE));
    }
}
