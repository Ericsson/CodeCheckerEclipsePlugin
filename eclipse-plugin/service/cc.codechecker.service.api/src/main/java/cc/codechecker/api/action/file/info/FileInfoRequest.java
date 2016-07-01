package cc.codechecker.api.action.file.info;

import cc.ecl.action.AbstractServerRequest;

import com.google.common.base.MoreObjects;

import java.util.Objects;


public class FileInfoRequest extends AbstractServerRequest {

    private final long fileId;

    public FileInfoRequest(String server, long fileId) {
        super(server);
        this.fileId = fileId;
    }

    public long getFileId() {
        return fileId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("server", server).add("fileId", fileId)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, fileId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileInfoRequest && super.equals(obj)) {
            FileInfoRequest oth = (FileInfoRequest) obj;

            return Objects.equals(fileId, oth.fileId);
        }

        return false;
    }
}
