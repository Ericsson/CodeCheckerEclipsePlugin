package cc.ecl.action.mock;

import cc.ecl.action.Action;

public class ServerAddOneAction extends Action<ServerIntegerRequest, Integer> {

    public ServerAddOneAction(ServerIntegerRequest request) {
        super(request);
    }
}
