package cc.ecl.action.thrift;

import com.google.common.reflect.Reflection;
import com.google.common.reflect.TypeToken;

import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Provider for HTTP/JSON cc.ecl.action.thrift protocol instances.
 *
 * Uses internal cache for the same URL.
 */
public class ThriftTransportFactory implements ThriftCommunicationInterface {

    private final static Logger LOGGER = Logger.getLogger(ThriftTransportFactory.class.getName());

    private Map<String, TTransport> activeTransports;

    private Map<TypeToken, TServiceClientFactory<?>> clientFactoryMappings;

    public ThriftTransportFactory() {
        activeTransports = new HashMap<>();
        clientFactoryMappings = new HashMap<>();
    }

    protected TProtocol requestTransport(String url) throws TTransportException {

        // probably not thread safe, but we need it? Not atm.

        TTransport act;

        if (!activeTransports.containsKey(url)) {
            LOGGER.info("Creating new transport for: " + url);
            activeTransports.put(url, new THttpClient(url));
        }

        act = activeTransports.get(url);

        if (!act.isOpen()) {
            act.open();
        }
        // THINK: always create new protocol?
        return new TJSONProtocol(act);
    }


    /**
     * Creates a client based on the TypeToken pointing to a Thrift interface class.
     *
     * This method uses black magic, based on the structure and names of generated
     * cc.ecl.action.thrift classes. If that changes, this here breaks.
     *
     * @throws TTransportException    if transport creation fails (e.g. timeout)
     * @throws ThriftFactoryException if factory creation fails (probably bad ifaceType parameter,
     *                                or Thrift internals changed)
     */
    @Override
    public <IFaceT> IFaceT initializeClient(String url, TypeToken<IFaceT> ifaceType) throws
            TTransportException, ThriftFactoryException {

        // assume same class loader
        ClassLoader classLoader = ifaceType.getRawType().getClassLoader();
        String factoryName = ifaceType.toString().replace("Iface", "Client$Factory");

        try {

            if (!clientFactoryMappings.containsKey(ifaceType)) {
                clientFactoryMappings.put(ifaceType, (TServiceClientFactory<?>) classLoader
                        .loadClass(factoryName).newInstance());
            }
        } catch (Exception e) {
            throw new RuntimeException("IllegalAccessException while initializing: " +
                    factoryName, e);
        }

        return (IFaceT) clientFactoryMappings.get(ifaceType).getClient(requestTransport(url));
    }

    public static class ThriftFactoryException extends Exception {
        public ThriftFactoryException(String s) {
            super(s);
        }

        public ThriftFactoryException(String s, IllegalAccessException e) {
            super(s, e);
        }
    }
}
