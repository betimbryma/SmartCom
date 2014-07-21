package at.ac.tuwien.dsg.smartcom.broker.utils;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

import java.net.URI;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class ApacheActiveMQUtils {

    private static BrokerService broker;

    public synchronized static void startActiveMQ(int port) throws Exception {
        if (port < 0) {
            port = 61616;
        }
        if (broker != null) {
            broker.stop();
            broker.waitUntilStopped();
        }
        broker = BrokerFactory.createBroker(new URI("broker:tcp://localhost:"+port));
        broker.start();
        broker.waitUntilStarted();
    }

    public synchronized static void stopActiveMQ() throws Exception {
        if (broker != null) {
            broker.stop();
            broker.waitUntilStopped();
        }
    }
}
