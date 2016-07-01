package cc.codechecker.api.action.result.count;

import cc.ecl.action.Action;
import cc.ecl.action.ActionParameterInfo;

import com.google.common.reflect.TypeToken;

public class SearchCountAction extends Action<SearchCountRequest, ResultCount> {
    public SearchCountAction(SearchCountRequest request) {
        super(request);
    }

    public static ActionParameterInfo getStaticParameterInfo() {
        return new ActionParameterInfo(TypeToken.of(SearchCountRequest.class), TypeToken.of
                (ResultCount.class));
    }
}
