package cc.codechecker.api.action.result.count;

import cc.ecl.action.Action;
import cc.ecl.action.ActionParameterInfo;

import com.google.common.reflect.TypeToken;

public class ListCountAction extends Action<ListCountRequest, ResultCount> {
    public ListCountAction(ListCountRequest request) {
        super(request);
    }

    public static ActionParameterInfo getStaticParameterInfo() {
        return new ActionParameterInfo(TypeToken.of(ListCountRequest.class), TypeToken.of
                (ResultCount.class));
    }
}
