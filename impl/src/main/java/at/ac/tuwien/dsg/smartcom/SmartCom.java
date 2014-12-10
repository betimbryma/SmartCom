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
import at.ac.tuwien.dsg.smartcom.adapters.EmailOutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapters.RESTOutputAdapter;
import at.ac.tuwien.dsg.smartcom.broker.impl.ApacheActiveMQMessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.utils.ApacheActiveMQUtils;
import at.ac.tuwien.dsg.smartcom.callback.CollectiveInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerAuthenticationCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerInfoCallback;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.exception.ErrorCode;
import at.ac.tuwien.dsg.smartcom.manager.MessagingAndRoutingManager;
import at.ac.tuwien.dsg.smartcom.manager.am.AdapterExecutionEngine;
import at.ac.tuwien.dsg.smartcom.manager.am.AdapterManagerImpl;
import at.ac.tuwien.dsg.smartcom.manager.am.AddressResolver;
import at.ac.tuwien.dsg.smartcom.manager.auth.AuthenticationManagerImpl;
import at.ac.tuwien.dsg.smartcom.manager.auth.AuthenticationRequestHandler;
import at.ac.tuwien.dsg.smartcom.manager.auth.dao.MongoDBAuthenticationSessionDAO;
import at.ac.tuwien.dsg.smartcom.manager.dao.MongoDBPeerChannelAddressResolverDAO;
import at.ac.tuwien.dsg.smartcom.manager.messaging.MessagingAndRoutingManagerImpl;
import at.ac.tuwien.dsg.smartcom.manager.messaging.PeerInfoService;
import at.ac.tuwien.dsg.smartcom.manager.messaging.PeerInfoServiceImpl;
import at.ac.tuwien.dsg.smartcom.manager.messaging.logging.LoggingService;
import at.ac.tuwien.dsg.smartcom.manager.messaging.logging.dao.LoggingDAO;
import at.ac.tuwien.dsg.smartcom.manager.messaging.logging.dao.MongoDBLoggingDAO;
import at.ac.tuwien.dsg.smartcom.model.MessageLogLevel;
import at.ac.tuwien.dsg.smartcom.rest.CommunicationRESTImpl;
import at.ac.tuwien.dsg.smartcom.services.MessageInfoService;
import at.ac.tuwien.dsg.smartcom.services.MessageInfoServiceImpl;
import at.ac.tuwien.dsg.smartcom.services.MessageQueryService;
import at.ac.tuwien.dsg.smartcom.services.MessageQueryServiceImpl;
import at.ac.tuwien.dsg.smartcom.services.dao.MessageInfoDAO;
import at.ac.tuwien.dsg.smartcom.services.dao.MongoDBMessageInfoDAO;
import at.ac.tuwien.dsg.smartcom.services.dao.MongoDBMessageQueryDAO;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class SmartCom {
    private static final Logger log = LoggerFactory.getLogger(SmartCom.class);

    private Communication communication;
    private MessageInfoServiceImpl messageInfoService;
    private MessageQueryService queryService;

    private MutablePicoContainer pico;

    private CommunicationRESTImpl communicationREST;

    private final SmartComConfiguration configuration;

    public SmartCom(SmartComConfiguration configuration) throws CommunicationException {
        this.configuration = configuration;
    }

    public void initializeSmartCom() throws CommunicationException {
        log.info("Initializing SmartCom Communication Middleware...");
        pico = new PicoBuilder().withAnnotatedFieldInjection().withJavaEE5Lifecycle().withCaching().build();

        log.info("Adding external components...");
        //add external components
        pico.addComponent(PeerAuthenticationCallback.class, this.configuration.peerManager);
        pico.addComponent(CollectiveInfoCallback.class, this.configuration.collectiveInfoCallback);
        pico.addComponent(PeerInfoCallback.class, this.configuration.peerInfoCallback);

        log.info("Initializing components...");
        initComponents();

        log.info("Creating component instances...");
        this.communication = pico.getComponent(Communication.class);
        this.messageInfoService = (MessageInfoServiceImpl) pico.getComponent(MessageInfoService.class);
        this.queryService = pico.getComponent(MessageQueryService.class);

        log.info("Creating rest api...");
        initRESTAPI();

        pico.start();

        log.info("Adding default adapters...");
        if (this.configuration.initAdapters) {
            addDefaultAdapters();
        }

        log.info("Initialization of SmartCom Communication Middleware complete!");
    }

    private void initComponents() throws CommunicationException {
        pico.addComponent(CommunicationImpl.class);
        pico.addComponent(StatisticBean.class);

        initMessageBroker();
        initAdapterManager();
        initMessagingAndRouting();
        initAuthenticationManager();
        initMessageInfoService();
        initMessageQueryService();
    }

    private void addDefaultAdapters() throws CommunicationException {
        //Email adapter
        communication.registerOutputAdapter(EmailOutputAdapter.class);

        //Android adapter
        communication.registerOutputAdapter(AndroidOutputAdapter.class);

        //Dropbox adapter
        communication.registerOutputAdapter(DropboxOutputAdapter.class);

        //REST adapter
        communication.registerOutputAdapter(RESTOutputAdapter.class);
    }

    private void initMessageQueryService() throws CommunicationException {
        log.debug("Initializing message query service");
        pico.addComponent(new MongoDBMessageQueryDAO(this.configuration.mongoClient, this.configuration.mongoDBDatabaseName, "log"));
        pico.addComponent(MessageQueryServiceImpl.class);
    }

    private void initMessageInfoService() throws CommunicationException {
        log.debug("Initializing message info service");

        pico.addComponent(new MongoDBMessageInfoDAO(this.configuration.mongoClient, this.configuration.mongoDBDatabaseName, "mis"));
        messageInfoService = new MessageInfoServiceImpl(this.configuration.misAPIPort, "", pico.getComponent(MessageInfoDAO.class));
        messageInfoService.init();
        pico.addComponent(MessageInfoService.class, messageInfoService);
    }

    private void initAuthenticationManager() throws CommunicationException {
        log.debug("Initializing authentication manager");
        pico.addComponent(new MongoDBAuthenticationSessionDAO(this.configuration.mongoClient, this.configuration.mongoDBDatabaseName, "session"));
        pico.addComponent(AuthenticationRequestHandler.class);
        pico.addComponent(AuthenticationManagerImpl.class);
    }

    private void initMessagingAndRouting() throws CommunicationException {
        log.debug("Initializing messaging and routing manager");

        //Messaging and Routing Manager
        pico.addComponent(MessageLogLevel.class, this.configuration.messageLogLevel);
        pico.addComponent(MessagingAndRoutingManager.class, MessagingAndRoutingManagerImpl.class);
        pico.addComponent(PeerInfoService.class, PeerInfoServiceImpl.class);

        //Logging
        pico.addComponent(LoggingDAO.class, new MongoDBLoggingDAO(this.configuration.mongoClient, this.configuration.mongoDBDatabaseName, "logging"));
        pico.addComponent(LoggingService.class);
    }

    private void initAdapterManager() throws CommunicationException {
        log.debug("Initializing adapter manager");
        pico.as(Characteristics.CACHE).addComponent(new MongoDBPeerChannelAddressResolverDAO(this.configuration.mongoClient, this.configuration.mongoDBDatabaseName, "resolver"));
        pico.as(Characteristics.CACHE).addComponent(AdapterManagerImpl.class);
        pico.as(Characteristics.CACHE).addComponent(AdapterExecutionEngine.class);
        pico.as(Characteristics.CACHE).addComponent(AddressResolver.class);
    }

    private void initRESTAPI() {
        log.debug("Initializing REST API");
        communicationREST = new CommunicationRESTImpl(this.configuration.restAPIPort, "", communication, pico.getComponent(StatisticBean.class));
        communicationREST.init();
    }

    private ApacheActiveMQMessageBroker messageBroker;

    private void initMessageBroker() throws CommunicationException {
        log.debug("Initializing message broker");
        if (this.configuration.initActiveMQ) {
            try {
                ApacheActiveMQUtils.startActiveMQWithoutPersistence(this.configuration.activeMQPort); //uses standard port
            } catch (Exception e) {
                throw new CommunicationException(e, new ErrorCode(10, "Could not initialize message broker"));
            }
        }
        messageBroker = new ApacheActiveMQMessageBroker(this.configuration.activeMqHost, this.configuration.activeMQPort, this.configuration.useLocalMQ, pico.getComponent(StatisticBean.class));
        pico.addComponent(messageBroker);
//        pico.addComponent(MessageBroker.class, SimpleMessageBroker.class); //enables this line and disable the ones above for a fast local execution
    }

    public void tearDownSmartCom() throws CommunicationException {
        communicationREST.cleanUp();
        messageInfoService.cleanUp();

        pico.stop();
        pico.dispose();

        if (messageBroker != null) {
            messageBroker.cleanUp();
        }

        this.configuration.mongoClient.close();

        if (this.configuration.mongoDB != null) {
            this.configuration.mongoDB.tearDown();
        }

        if (this.configuration.initActiveMQ) {
            try {
                ApacheActiveMQUtils.stopActiveMQ();
            } catch (Exception e) {
                throw new CommunicationException(e, new ErrorCode(10, "Could not shutdown message broker"));
            }
        }
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
