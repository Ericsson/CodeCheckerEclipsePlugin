package cc.codechecker.api.action.result.list;

import cc.codechecker.api.action.result.ResultFilter;
import cc.ecl.action.AbstractServerRequest;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.Objects;

public class SearchRequest extends AbstractServerRequest {

    private final Optional<Long> id;
    private final long beginId;
    private final long endId;
    private final ImmutableList<ResultFilter> resultFilters;

    public SearchRequest(String serverUrl, Optional<Long> id, long beginId, long endId,
                         ImmutableList<ResultFilter> resultFilters) {
        super(serverUrl);

        this.id = id;
        this.beginId = beginId;
        this.endId = endId;
        this.resultFilters = resultFilters;
    }

    public Optional<Long> getId() {
        return id;
    }

    public long getBeginId() {
        return beginId;
    }

    public long getEndId() {
        return endId;
    }

    public ImmutableList<ResultFilter> getResultFilters() {
        return resultFilters;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("beginId", beginId).add
                ("endId", endId).add("filters", resultFilters).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, id, beginId, endId, resultFilters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SearchRequest && super.equals(obj)) {
            SearchRequest oth = (SearchRequest) obj;

            return Objects.equals(id, oth.id) && Objects.equals(beginId, oth.beginId) && Objects
                    .equals(endId, oth.endId) && Objects.equals(resultFilters, oth.resultFilters);
        }

        return false;
    }

    public enum Severity {
        STYLE(0),
        LOW(1),
        MEDIUM(2),
        HIGH(3),
        CRITICAL(4),
        ANY(5);

        private final int value;

        private Severity(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
