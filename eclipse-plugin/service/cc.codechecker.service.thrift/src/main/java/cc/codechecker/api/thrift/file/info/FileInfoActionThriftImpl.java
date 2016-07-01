package cc.codechecker.api.thrift.file.info;

import cc.codechecker.api.action.file.info.FileInfo;
import cc.codechecker.api.action.file.info.FileInfoRequest;
import cc.codechecker.service.thrift.gen.CodeCheckerDBAccess;
import cc.codechecker.service.thrift.gen.SourceFileData;
import cc.ecl.action.Action;
import cc.ecl.action.ActionCommImpl;
import cc.ecl.action.ActionResult;
import cc.ecl.action.InnerRunner;
import cc.ecl.action.thrift.ThriftActionImpl;
import cc.ecl.action.thrift.ThriftCommunicationInterface;

import org.apache.thrift.TException;

public class FileInfoActionThriftImpl extends ThriftActionImpl<FileInfoRequest, FileInfo,
        CodeCheckerDBAccess.Iface> {
    @Override
    protected String getProtocolUrlEnd(FileInfoRequest request) {
        return "codeCheckerDBAccess";
    }

    @Override
    protected ActionResult<FileInfo> runThrift(CodeCheckerDBAccess.Iface client,
                                               Action<FileInfoRequest, FileInfo> action,
                                               InnerRunner innerRunner) throws TException {

        SourceFileData d = client.getSourceFileData(action.getRequest().getFileId(), true);

        return new ActionResult<>(new FileInfo(d.getFileId(), d.getFilePath(), d.getFileContent()));
    }

    @Override
    public ActionCommImpl<FileInfoRequest, FileInfo, ThriftCommunicationInterface> dup() {
        return new FileInfoActionThriftImpl();
    }
}
