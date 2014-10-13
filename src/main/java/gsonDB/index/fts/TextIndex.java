package gsonDB.index.fts;

import java.util.Set;

/**
 * Created by Sleiman on 13/10/2014.
 */
public interface TextIndex {

    /***
     * @param documentId
     * @param tokens, a line of string tokens to be indexed, ex: This is a fantastic indexer
     */
    void index(long documentId, String tokens);

    /***
     * @param token, a string to search
     * @return set of document ids
     */
    Set<Long> search(String token);
}
