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

    public synchronized static void startActiveMQWithoutPersistence(int port) throws Exception {
        startActiveMQ(port, false);
    }

    public synchronized static void startActiveMQ(int port) throws Exception {
        startActiveMQ(port, true);
    }

    private static void startActiveMQ(int port, boolean persistence) throws Exception {
        if (port < 0) {
            port = 61616;
        }
        if (broker != null) {
            broker.stop();
            broker.waitUntilStopped();
        }
        broker = BrokerFactory.createBroker(new URI("broker:tcp://localhost:"+port));
        broker.setPersistent(persistence);

        broker.deleteAllMessages();
        broker.start();
        broker.waitUntilStarted();
    }

    public synchronized static void stopActiveMQ() throws Exception {
        if (broker != null) {
            broker.deleteAllMessages();
            broker.stop();
            broker.waitUntilStopped();
        }
    }
}
