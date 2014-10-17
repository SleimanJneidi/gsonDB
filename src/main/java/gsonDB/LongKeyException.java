package gsonDB;

import gsonDB.index.PrimaryIndexProcessor;

/**
 * Created by Sleiman on 02/10/2014.
 */
public class LongKeyException extends RuntimeException {

    public LongKeyException(){
        super(String.format("Key exceeds the maximum key length: %d bytes", PrimaryIndexProcessor.KEY_SIZE));
    }
}
