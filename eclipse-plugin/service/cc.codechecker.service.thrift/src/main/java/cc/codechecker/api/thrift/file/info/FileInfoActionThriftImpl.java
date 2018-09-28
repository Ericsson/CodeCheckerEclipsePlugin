package cc.codechecker.api.thrift.file.info;

import cc.codechecker.api.action.file.info.FileInfo;
import cc.codechecker.api.action.file.info.FileInfoRequest;
import cc.codechecker.service.thrift.gen.codeCheckerDBAccess;
import cc.codechecker.service.thrift.gen.SourceFileData;
import cc.codechecker.service.thrift.gen.Encoding;
import cc.ecl.action.Action;
import cc.ecl.action.ActionCommImpl;
import cc.ecl.action.ActionResult;
import cc.ecl.action.InnerRunner;
import cc.ecl.action.thrift.ThriftActionImpl;
import cc.ecl.action.thrift.ThriftCommunicationInterface;

import org.apache.thrift.TException;

public class FileInfoActionThriftImpl extends ThriftActionImpl<FileInfoRequest, FileInfo,
        codeCheckerDBAccess.Iface> {
    @Override
    protected String getProtocolUrlEnd(FileInfoRequest request) {
        return "CodeCheckerService";
    }

    @Override
    protected ActionResult<FileInfo> runThrift(codeCheckerDBAccess.Iface client,
                                               Action<FileInfoRequest, FileInfo> action,
                                               InnerRunner innerRunner) throws TException {

        SourceFileData d = client.getSourceFileData(action.getRequest().getFileId(), true, Encoding.DEFAULT);

        return new ActionResult<>(new FileInfo(d.getFileId(), d.getFilePath(), d.getFileContent()));
    }

    @Override
    public ActionCommImpl<FileInfoRequest, FileInfo, ThriftCommunicationInterface> dup() {
        return new FileInfoActionThriftImpl();
    }
}
