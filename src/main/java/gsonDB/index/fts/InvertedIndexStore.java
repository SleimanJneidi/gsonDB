package gsonDB.index.fts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Sleiman on 12/10/2014.
 */
public interface InvertedIndexStore {

    Map<String, List<IndexTuple>> load(String stringOfTokens);

    Map<String, List<IndexTuple>> load();

    void store(Map<String, List<IndexTuple>> invertedIndexMap);
}
