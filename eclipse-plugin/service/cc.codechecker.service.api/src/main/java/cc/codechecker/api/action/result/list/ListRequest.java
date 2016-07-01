package cc.codechecker.api.action.result.list;

import cc.ecl.action.AbstractServerRequest;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class ListRequest extends AbstractServerRequest {

    private final long beginId;

    private final long endId;

    private final String filename;

    public ListRequest(String serverUrl, long beginId, long endId, String file) {
        super(serverUrl);

        this.beginId = beginId;
        this.endId = endId;
        this.filename = file;
    }

    public long getBeginId() {
        return beginId;
    }

    public long getEndId() {
        return endId;
    }

    public String getFileList() {
        return filename;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("beginId", beginId).add("endId", endId).add
                ("filename", filename).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, beginId, endId, filename);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ListRequest && super.equals(obj)) {
            ListRequest oth = (ListRequest) obj;

            return Objects.equals(beginId, oth.beginId) && Objects.equals(endId, oth.endId) &&
                    Objects.equals(filename, oth.filename);
        }

        return false;
    }
}
