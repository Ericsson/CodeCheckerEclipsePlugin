package cc.codechecker.api.action.analyze;

import cc.ecl.action.AbstractServerRequest;

import com.google.common.collect.ImmutableList;

public class AnalyzeRequest extends AbstractServerRequest {

    private final ImmutableList<String> fileList;

    public AnalyzeRequest(String server, ImmutableList<String> fileList) {
        super(server);
        this.fileList = fileList;
    }

    public ImmutableList<String> getFileList() {
        return fileList;
    }
}
