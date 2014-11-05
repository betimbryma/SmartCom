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
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.exception.ErrorCode;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.MongoClient;

import java.io.IOException;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class SmartComBuilder {
    private final SmartComConfiguration configuration;

    public SmartComBuilder(PeerAuthenticationCallback peerManager, PeerInfoCallback peerInfoCallback, CollectiveInfoCallback collectiveInfoCallback) {
        this.configuration = new SmartComConfiguration();
        this.configuration.peerManager = peerManager;
        this.configuration.peerInfoCallback = peerInfoCallback;
        this.configuration.collectiveInfoCallback = collectiveInfoCallback;
    }

    public SmartComBuilder initializeActiveMQ(boolean initActiveMQ) {
        this.configuration.initActiveMQ = initActiveMQ;
        return this;
    }

    public SmartComBuilder setActiveMqPort(int port) {
        this.configuration.activeMQPort = port;
        return this;
    }

    public SmartComBuilder setActiveMqHost(String activeMqHost) {
        this.configuration.activeMqHost = activeMqHost;
        return this;
    }

    public SmartComBuilder setRestApiPort(int port) {
        this.configuration.restAPIPort = port;
        return this;
    }

    public SmartComBuilder setMongoClient(MongoClient client) {
        this.configuration.mongoClient = client;
        return this;
    }

    public SmartComBuilder setMongoDBInstance(MongoDBInstance instance) {
        this.configuration.mongoDB = instance;
        return this;
    }

    public SmartComBuilder initAdapters(boolean initAdapters) {
        this.configuration.initAdapters = initAdapters;
        return this;
    }

    public SmartComBuilder useLocalMessageQueue(boolean useLocalMQ) {
        this.configuration.useLocalMQ = useLocalMQ;
        return this;
    }

    public SmartComBuilder setMongoDBDatabaseName(String databaseName) {
        this.configuration.mongoDBDatabaseName = databaseName;
        return this;
    }

    public SmartCom create() throws CommunicationException {
        if (this.configuration.mongoClient == null) {
            try {
                if (this.configuration.mongoDB == null) {
                    this.configuration.mongoDB = new MongoDBInstance(-1); //uses standard port
                    this.configuration.mongoDB.setUp();
                }

                this.configuration.mongoClient = this.configuration.mongoDB.getClient();
            } catch (IOException e) {
                throw new CommunicationException(e, new ErrorCode(1, "Could not create mongo database"));
            }
        }

        SmartCom smartCom = new SmartCom(configuration);
        smartCom.initializeSmartCom();

        return smartCom;
    }
}
