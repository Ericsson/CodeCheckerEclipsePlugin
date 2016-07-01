package cc.codechecker.api.action.result.count;

import cc.ecl.action.AbstractServerRequest;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class ListCountRequest extends AbstractServerRequest {


    private final String filename;

    public ListCountRequest(String serverUrl, String file) {
        super(serverUrl);

        this.filename = file;
    }

    public String getFileList() {
        return filename;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("filename", filename).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, filename);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ListCountRequest && super.equals(obj)) {
            ListCountRequest oth = (ListCountRequest) obj;

            return Objects.equals(filename, oth.filename);
        }

        return false;
    }
}
