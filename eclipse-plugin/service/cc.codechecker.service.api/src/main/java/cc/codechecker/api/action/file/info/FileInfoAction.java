package cc.codechecker.api.action.file.info;

import cc.ecl.action.Action;

public class FileInfoAction extends Action<FileInfoRequest, FileInfo> {
    public FileInfoAction(FileInfoRequest request) {
        super(request);
    }
}
