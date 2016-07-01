package cc.codechecker.api.action.result.list;

import cc.ecl.action.Action;
import cc.ecl.action.ActionParameterInfo;

import com.google.common.reflect.TypeToken;

public class ListAction extends Action<ListRequest, ResultList> {
    public ListAction(ListRequest request) {
        super(request);
    }

    public static ActionParameterInfo getStaticParameterInfo() {
        return new ActionParameterInfo(TypeToken.of(ListRequest.class), TypeToken.of(ResultList
                .class));
    }

}
