package cc.codechecker.api.job.report.list;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class ListRequest {

    private final String server;
    private final String filename;

    public ListRequest(String server, String filename) {
        this.server = server;
        this.filename = filename;
    }

    public String getServer() {
        return server;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("server", server).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(server);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ListRequest) {
            ListRequest oth = (ListRequest) obj;

            return Objects.equals(server, oth.server) && Objects.equals(filename, oth.filename);
        }

        return false;
    }
}
