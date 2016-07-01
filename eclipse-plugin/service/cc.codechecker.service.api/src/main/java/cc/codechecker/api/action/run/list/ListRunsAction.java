package cc.codechecker.api.action.run.list;

import cc.ecl.action.Action;
import cc.ecl.action.ActionParameterInfo;

import com.google.common.reflect.TypeToken;

public class ListRunsAction extends Action<ListRunsRequest, RunList> {

    public ListRunsAction(ListRunsRequest request) {
        super(request);
    }

    public static ActionParameterInfo getStaticParameterInfo() {
        return new ActionParameterInfo(TypeToken.of(ListRunsRequest.class), TypeToken.of(RunList
                .class));
    }
}
