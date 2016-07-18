package cc.ecl.action.thrift;

import cc.ecl.action.*;

import com.google.common.reflect.TypeToken;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;

/**
 * Basic implementation for HTTP/JSON Thrift actions.
 */
public abstract class ThriftActionImpl<ReqT extends ServerRequest, ResT, IFaceT> extends
        AbstractActionImpl<ReqT, ResT, ThriftCommunicationInterface> {

    private final static Logger logger = LogManager.getLogger(ThriftActionImpl.class.getName());

    protected final TypeToken<IFaceT> ifaceType = new TypeToken<IFaceT>(getClass()) {
    };

    @Override
    @SuppressWarnings("unchecked")
    protected ActionResult<ResT> doRealRun(Action<ReqT, ResT> action, InnerRunner innerRunner,
                                           ThriftCommunicationInterface communicationInterface) {
        try {
            IFaceT iface = communicationInterface.initializeClient(action.getRequest().getServer
                    () + "/" + getProtocolUrlEnd(action.getRequest()), ifaceType);

            return runThrift(iface, action, innerRunner);
        } catch (TTransportException tte) {
        	logger.log(Level.ERROR, "Error in cc.ecl.action.thrift transport creation", tte);
            return new ActionResult<>(ActionStatus.COMMUNICATION_ERROR);
        } catch (TException te) {
        	logger.log(Level.ERROR, "Error in cc.ecl.action.thrift call", te);
            return new ActionResult<>(ActionStatus.COMMUNICATION_ERROR);
        } catch (ThriftTransportFactory.ThriftFactoryException e) {
        	logger.log(Level.ERROR, "Error in cc.ecl.action.thrift factory creation", e);
            return new ActionResult<>(ActionStatus.LOGIC_ERROR);
        }
    }

    // HTTP/JSON requires different URLs
    protected abstract String getProtocolUrlEnd(ReqT request);

    protected abstract ActionResult<ResT> runThrift(IFaceT client, Action<ReqT, ResT> action,
                                                    InnerRunner innerRunner) throws TException;
}
