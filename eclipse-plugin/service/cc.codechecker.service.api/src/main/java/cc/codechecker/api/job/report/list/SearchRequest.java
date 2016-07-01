package cc.codechecker.api.job.report.list;

import cc.codechecker.api.action.result.ResultFilter;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.Objects;

public class SearchRequest {

    private final String server;
    private final Optional<Long> id;
    private final ImmutableList<ResultFilter> filters;

    public SearchRequest(String server, Optional<Long> id, ImmutableList<ResultFilter> filters) {
        this.server = server;
        this.id = id;
        this.filters = filters;
    }

    public ImmutableList<ResultFilter> getFilters() {
        return filters;
    }

    public String getServer() {
        return server;
    }

    public Optional<Long> getId() {
        return id;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("server", server).add("id", id).add
                ("filters", filters).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, id, filters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SearchRequest) {
            SearchRequest oth = (SearchRequest) obj;

            return Objects.equals(server, oth.server) && Objects.equals(id, oth.id) && Objects
                    .equals(filters, oth.filters);
        }

        return false;
    }
}
