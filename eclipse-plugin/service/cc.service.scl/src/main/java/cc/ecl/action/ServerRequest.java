package cc.ecl.action;

/**
 * Request class which contains some server information.
 *
 * It's called server because most of the time it's a server, but it can be somthing else too, like
 * a local directory.
 *
 * @todo Consider a better way to handle this
 */
public interface ServerRequest {

    public String getServer();
}
