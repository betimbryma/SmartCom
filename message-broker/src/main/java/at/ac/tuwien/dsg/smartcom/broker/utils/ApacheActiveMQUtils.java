/**
 * Copyright (c) 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184 (http://dsg.tuwien.ac.at)
 *
 * This work was partially supported by the EU FP7 FET SmartSociety (http://www.smart-society-project.eu/).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
