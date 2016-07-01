package cc.ecl.action.thrift;

import com.google.common.reflect.TypeToken;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class ThriftTransportFactoryTest {

    @Test
    public void testFactoryCreation() throws ThriftTransportFactory.ThriftFactoryException,
            TTransportException, IllegalAccessException, InstantiationException {
        ThriftTransportFactory fac = new ThriftTransportFactory();

        TypeToken<ThriftService.Iface> tt = new TypeToken<ThriftService.Iface>() {
        };

        ThriftService.Iface client = fac.initializeClient("http://test.something", tt);

        assertThat(client, is(instanceOf(ThriftService.Client.class)));
    }

    /**
     * Crude sample implemntation of a cc.ecl.action.thrift service, just the parts needed by the
     * TransportFactory
     */
    public static class ThriftService {
        public interface Iface {
        }

        public static class Client extends TServiceClient implements Iface {
            public Client(TProtocol prot) {
                super(prot);
            }

            public static class Factory implements TServiceClientFactory<Client> {

                @Override
                public Client getClient(TProtocol tProtocol) {
                    return new Client(tProtocol);
                }

                @Override
                public Client getClient(TProtocol tProtocol, TProtocol tProtocol1) {
                    return null;
                }
            }
        }
    }

}
