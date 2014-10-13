package gsonDB.index.fts;

import java.io.Serializable;

/**
 * Created by Sleiman on 12/10/2014.
 */
public class IndexTuple implements Serializable {

    private final long documentId;
    private final int position;

    public IndexTuple(long documentId, int position) {
        this.documentId = documentId;
        this.position = position;
    }

    public long getDocumentId() {
        return documentId;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexTuple that = (IndexTuple) o;

        if (documentId != that.documentId) return false;
        if (position != that.position) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (documentId ^ (documentId >>> 32));
        result = 31 * result + position;
        return result;
    }
}
