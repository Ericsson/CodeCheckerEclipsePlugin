package cc.ecl.action;

/**
 * Implements the methods required by ServerRequest. For more information, see the interface.
 */
public abstract class AbstractServerRequest implements ServerRequest {

    protected final String server;

    protected AbstractServerRequest(String server) {
        this.server = server;
    }

    @Override
    public String getServer() {
        return server;
    }

    @Override
    public int hashCode() {
        return server.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractServerRequest)) {
            return false;
        }
        AbstractServerRequest asr = (AbstractServerRequest) obj;

        return asr.getServer().equals(getServer());
    }
}
