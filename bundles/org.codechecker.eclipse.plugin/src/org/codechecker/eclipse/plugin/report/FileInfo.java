package org.codechecker.eclipse.plugin.report;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class FileInfo {

    private final long fileId;
    private final String filePath;
    private final String fileContent;

    public FileInfo(long fileId, String filePath, String fileContent) {
        this.fileId = fileId;
        this.filePath = filePath;
        this.fileContent = fileContent;
    }

    public long getFileId() {
        return fileId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileContent() {
        return fileContent;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("fileId", fileContent).add("filePath",
                filePath).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileInfo) {
            FileInfo oth = (FileInfo) obj;

            return Objects.equals(fileId, oth.fileId);
        }

        return false;
    }
}
