package at.ac.tuwien.dsg.smartcom.broker.utils;

import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.exception.ErrorCode;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class BrokerErrorUtils {

    public static CommunicationException createBrokerException(Throwable t) {
        return new CommunicationException(t, new ErrorCode(100, "An error occurred in the broker!"));
    }

    public static RuntimeException createRuntimeBrokerException(Throwable t) {
        return new RuntimeException(new CommunicationException(t, new ErrorCode(199, "Fatal error in message broker! Can't continue execution!")));
    }
}
