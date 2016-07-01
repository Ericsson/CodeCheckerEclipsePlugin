package cc.codechecker.api.action.analyze;

import cc.ecl.action.Action;

public class AnalyzeAction extends Action<AnalyzeRequest, AnalyzeResult> {

    public AnalyzeAction(AnalyzeRequest request) {
        super(request);
    }
}
