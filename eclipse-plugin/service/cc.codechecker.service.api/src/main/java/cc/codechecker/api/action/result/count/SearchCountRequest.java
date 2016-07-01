package cc.codechecker.api.action.result.count;

import cc.codechecker.api.action.result.ResultFilter;
import cc.ecl.action.AbstractServerRequest;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.Objects;

public class SearchCountRequest extends AbstractServerRequest {

    private final Optional<Long> id;
    private final ImmutableList<ResultFilter> resultFilters;

    public SearchCountRequest(String serverUrl, Optional<Long> id, ImmutableList<ResultFilter>
            resultFilters) {
        super(serverUrl);

        this.id = id;
        this.resultFilters = resultFilters;
    }

    public Optional<Long> getId() {
        return id;
    }

    public ImmutableList<ResultFilter> getResultFilters() {
        return resultFilters;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("filters", resultFilters)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, id, resultFilters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SearchCountRequest && super.equals(obj)) {
            SearchCountRequest oth = (SearchCountRequest) obj;

            return Objects.equals(id, oth.id) && Objects.equals(resultFilters, oth.resultFilters);
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
