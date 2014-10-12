package gsonDB.index.fts;

import java.io.Serializable;

/**
 * Created by Sleiman on 12/10/2014.
 */
public class IndexTuple implements Serializable {

    private final String documentId;
    private final int position;

    public IndexTuple(String documentId, int position) {
        this.documentId = documentId;
        this.position = position;
    }

    public String getDocumentId() {
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

        if (position != that.position) return false;
        if (documentId != null ? !documentId.equals(that.documentId) : that.documentId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = documentId != null ? documentId.hashCode() : 0;
        result = 31 * result + position;
        return result;
    }


}
