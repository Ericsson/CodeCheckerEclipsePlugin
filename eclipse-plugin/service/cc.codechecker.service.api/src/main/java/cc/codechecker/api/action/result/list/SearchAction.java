package cc.codechecker.api.action.result.list;

import cc.ecl.action.Action;
import cc.ecl.action.ActionParameterInfo;

import com.google.common.reflect.TypeToken;

public class SearchAction extends Action<SearchRequest, ResultList> {
    public SearchAction(SearchRequest request) {
        super(request);
    }

    public static ActionParameterInfo getStaticParameterInfo() {
        return new ActionParameterInfo(TypeToken.of(SearchRequest.class), TypeToken.of(ResultList
                .class));
    }

}
