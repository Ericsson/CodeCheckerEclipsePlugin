package cc.ecl.action;

import com.google.common.reflect.TypeToken;

/**
 * Contains type information about the class generic parameters, to hack java's "generic" type
 * information.
 *
 * @todo: create a compatibleWith method?
 */
public class ActionParameterInfo {

    private TypeToken<?> requestType;
    private TypeToken<?> resultType;

    public ActionParameterInfo(TypeToken<?> requestType, TypeToken<?> resultType) {
        this.requestType = requestType;
        this.resultType = resultType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ActionParameterInfo)) {
            return false;
        }
        ActionParameterInfo api = (ActionParameterInfo) obj;

        return api.requestType.equals(requestType) && api.resultType.equals(resultType);
    }

    @Override
    public int hashCode() {
        return requestType.hashCode() + resultType.hashCode();
    }

    @Override
    public String toString() {
        return "[ActionType: " + requestType.getRawType().getName() + " -> " + resultType
                .getRawType().getName() + "]";
    }
}
