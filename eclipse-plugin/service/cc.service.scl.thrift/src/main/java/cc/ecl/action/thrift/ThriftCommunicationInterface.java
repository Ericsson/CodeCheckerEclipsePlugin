package cc.ecl.action.thrift;

import com.google.common.reflect.TypeToken;

import org.apache.thrift.transport.TTransportException;

/**
 * Required methods for HTTP/JSON Thrift communication
 */
public interface ThriftCommunicationInterface {

    <IFaceT> IFaceT initializeClient(String url, TypeToken<IFaceT> ifaceType) throws
            TTransportException, ThriftTransportFactory.ThriftFactoryException;
}
