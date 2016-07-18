package cc.codechecker.api.thrift;

import cc.codechecker.api.thrift.file.info.FileInfoActionThriftImpl;
import cc.codechecker.api.thrift.run.list.ListRunsActionThriftImpl;
import cc.codechecker.api.thrift.result.count.ResultCountActionThriftImpl;
import cc.codechecker.api.thrift.result.list.ResultListActionThriftImpl;
import cc.ecl.action.ActionImplementationRegistry;

public class CodecheckerActionInitializer {

    public ActionImplementationRegistry initialize(ActionImplementationRegistry reg) {

        reg.addImplementation(new ListRunsActionThriftImpl());
        reg.addImplementation(new FileInfoActionThriftImpl());
        reg.addImplementation(new ResultListActionThriftImpl());
        reg.addImplementation(new ResultCountActionThriftImpl());

        return reg;
    }
}
