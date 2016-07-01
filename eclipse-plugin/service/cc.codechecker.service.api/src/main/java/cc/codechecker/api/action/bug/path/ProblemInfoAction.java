package cc.codechecker.api.action.bug.path;

import cc.ecl.action.Action;

public class ProblemInfoAction extends Action<ProblemInfoRequest, ProblemInfo> {
    public ProblemInfoAction(ProblemInfoRequest request) {
        super(request);
    }
}
