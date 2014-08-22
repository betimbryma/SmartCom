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

import at.ac.tuwien.dsg.smartcom.adapters.AndroidOutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapters.DropboxOutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapters.RESTOutputAdapter;
import at.ac.tuwien.dsg.smartcom.broker.impl.ApacheActiveMQMessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.utils.ApacheActiveMQUtils;
import at.ac.tuwien.dsg.smartcom.callback.CollectiveInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerAuthenticationCallback;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.exception.ErrorCode;
import at.ac.tuwien.dsg.smartcom.manager.am.AdapterExecutionEngine;
import at.ac.tuwien.dsg.smartcom.manager.am.AdapterManagerImpl;
import at.ac.tuwien.dsg.smartcom.manager.am.AddressResolver;
import at.ac.tuwien.dsg.smartcom.manager.auth.AuthenticationManagerImpl;
import at.ac.tuwien.dsg.smartcom.manager.auth.AuthenticationRequestHandler;
import at.ac.tuwien.dsg.smartcom.manager.auth.dao.MongoDBAuthenticationSessionDAO;
import at.ac.tuwien.dsg.smartcom.manager.dao.MongoDBPeerChannelAddressResolverDAO;
import at.ac.tuwien.dsg.smartcom.messaging.logging.LoggingService;
import at.ac.tuwien.dsg.smartcom.messaging.logging.dao.LoggingDAO;
import at.ac.tuwien.dsg.smartcom.messaging.logging.dao.MongoDBLoggingDAO;
import at.ac.tuwien.dsg.smartcom.services.MessageInfoService;
import at.ac.tuwien.dsg.smartcom.services.MessageQueryService;
import at.ac.tuwien.dsg.smartcom.services.MessageQueryServiceImpl;
import at.ac.tuwien.dsg.smartcom.services.dao.MongoDBMessageQueryDAO;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.MongoClient;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class SmartCom {
    private static final Logger log = LoggerFactory.getLogger(SmartCom.class);

    public static final String MONGODB_DATABASE = "SmartCom";
    public static final int ACTIVE_MQ_PORT = 61616;

    private Communication communication;
    private MessageInfoService messageInfoService;
    private MessageQueryService queryService;

    private final PeerAuthenticationCallback peerManager;
    private final CollectiveInfoCallback collectiveInfoCallback;

    private MutablePicoContainer pico;
    private MongoDBInstance mongoDB;
    private MongoClient mongoClient;

    public SmartCom(PeerAuthenticationCallback peerManager, CollectiveInfoCallback collectiveInfoCallback) {
        this.peerManager = peerManager;
        this.collectiveInfoCallback = collectiveInfoCallback;
    }

    public void initializeSmartCom() throws CommunicationException {
        log.info("Initializing SmartCom Communication Middleware...");
        pico = new PicoBuilder().withAnnotatedFieldInjection().withJavaEE5Lifecycle().withCaching().build();

        log.info("Adding external components...");
        //add external components
        pico.addComponent(peerManager);
        pico.addComponent(collectiveInfoCallback);

        //start database
        log.info("Starting databases...");
        try {
            mongoDB = new MongoDBInstance(-1); //uses standard port
            mongoDB.setUp();
            mongoClient = mongoDB.getClient();
        } catch (IOException e) {
            throw new CommunicationException(e, new ErrorCode(1, "Could not create mongo database"));
        }

        log.info("Initializing components...");
        initComponents();

        log.info("Creating component instances...");
        this.communication = pico.getComponent(Communication.class);
        this.messageInfoService = pico.getComponent(MessageInfoService.class);
        this.queryService = pico.getComponent(MessageQueryService.class);

        pico.start();

        log.info("Adding default adapters...");
        addDefaultAdapters();

        log.info("Initialization of SmartCom Communication Middleware complete!");
    }

    private void initComponents() throws CommunicationException {
        pico.addComponent(CommunicationImpl.class);

        initMessageBroker();
        initAdapterManager();
        initMessagingAndRouting();
        initAuthenticationManager();
        initMessageInfoService();
        initMessageQueryService();
    }

    private void addDefaultAdapters() throws CommunicationException {
        //TODO

        //Android adapter
        communication.registerOutputAdapter(AndroidOutputAdapter.class);

        //Dropbox adapter
        communication.registerOutputAdapter(DropboxOutputAdapter.class);

        //REST adapter
        communication.registerOutputAdapter(RESTOutputAdapter.class);
    }

    private void initMessageQueryService() throws CommunicationException {
        log.debug("Initializing message query service");
        pico.addComponent(new MongoDBMessageQueryDAO(mongoClient, MONGODB_DATABASE, "log"));
        pico.addComponent(MessageQueryServiceImpl.class);
    }

    private void initMessageInfoService() throws CommunicationException {
        log.debug("Initializing message info service");
        //TODO
    }

    private void initAuthenticationManager() throws CommunicationException {
        log.debug("Initializing authentication manager");
        pico.addComponent(new MongoDBAuthenticationSessionDAO(mongoClient, MONGODB_DATABASE, "session"));
        pico.addComponent(AuthenticationRequestHandler.class);
        pico.addComponent(AuthenticationManagerImpl.class);
    }

    private void initMessagingAndRouting() throws CommunicationException {
        log.debug("Initializing messaging and routing manager");
        //TODO

        //Logging
        pico.addComponent(LoggingDAO.class, new MongoDBLoggingDAO(mongoClient, MONGODB_DATABASE, "logging"));
        pico.addComponent(LoggingService.class);
    }

    private void initAdapterManager() throws CommunicationException {
        log.debug("Initializing adapter manager");
        pico.as(Characteristics.CACHE).addComponent(new MongoDBPeerChannelAddressResolverDAO(mongoClient, MONGODB_DATABASE, "resolver"));
        pico.as(Characteristics.CACHE).addComponent(AdapterManagerImpl.class);
        pico.as(Characteristics.CACHE).addComponent(AdapterExecutionEngine.class);
        pico.as(Characteristics.CACHE).addComponent(AddressResolver.class);
    }

    private void initMessageBroker() throws CommunicationException {
        log.debug("Initializing message broker");
        try {
            ApacheActiveMQUtils.startActiveMQ(ACTIVE_MQ_PORT); //uses standard port
        } catch (Exception e) {
            throw new CommunicationException(e, new ErrorCode(10, "Could not initialize message broker"));
        }
        pico.addComponent(new ApacheActiveMQMessageBroker("localhost", ACTIVE_MQ_PORT));
    }

    public void tearDownSmartCom() throws CommunicationException {
        try {
            ApacheActiveMQUtils.stopActiveMQ();
        } catch (Exception e) {
            throw new CommunicationException(e, new ErrorCode(99, "Could not stop ActiveMQ"));
        }

        pico.stop();
        pico.dispose();

        mongoClient.close();
        mongoDB.tearDown();
    }

    public Communication getCommunication() {
        return communication;
    }

    public MessageInfoService getMessageInfoService() {
        return messageInfoService;
    }

    public MessageQueryService getQueryService() {
        return queryService;
    }
}
