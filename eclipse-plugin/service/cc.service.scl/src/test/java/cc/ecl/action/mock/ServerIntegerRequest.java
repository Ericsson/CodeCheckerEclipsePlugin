package cc.ecl.action.mock;

import cc.ecl.action.ServerRequest;

public class ServerIntegerRequest implements ServerRequest {

    private final String server;

    private final Integer query;

    public ServerIntegerRequest(String server, Integer query) {
        this.server = server;
        this.query = query;
    }

    @Override
    public String getServer() {
        return server;
    }

    public Integer getQuery() {
        return query;
    }
}
