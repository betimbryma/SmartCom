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
package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.callback.CollectiveInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerAuthenticationCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerInfoCallback;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.MongoClient;

/**
* @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
* @version 1.0
*/
public class SmartComConfiguration {
    public static final String MONGODB_DATABASE = "SmartCom";
    public static final int ACTIVE_MQ_DEFAULT_PORT = 61616;
    public static final String ACTIVE_MQ_DEFAULT_HOST = "localhost";
    public static final int REST_API_DEFAULT_PORT = 8080;
    public static final boolean ADAPTER_INITIALISATION_DEFAULT = true;

    //Dependencies configuration
    PeerAuthenticationCallback peerManager;
    PeerInfoCallback peerInfoCallback;
    CollectiveInfoCallback collectiveInfoCallback;

    //MongoDB configuration
    MongoClient mongoClient;
    MongoDBInstance mongoDB;
    String mongoDBDatabaseName = MONGODB_DATABASE;

    //ActiveMQ configuration
    boolean useLocalMQ = false;
    boolean initActiveMQ = true;
    String activeMqHost = ACTIVE_MQ_DEFAULT_HOST;
    int activeMQPort = ACTIVE_MQ_DEFAULT_PORT;

    int restAPIPort = REST_API_DEFAULT_PORT;
    boolean initAdapters = ADAPTER_INITIALISATION_DEFAULT;
}
