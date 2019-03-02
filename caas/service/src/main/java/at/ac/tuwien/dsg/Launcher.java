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
package at.ac.tuwien.dsg;

import at.ac.tuwien.dsg.peer.PeerMailboxService;
import at.ac.tuwien.dsg.peer.PeerMailboxServiceLauncher;
import at.ac.tuwien.dsg.pm.PeerManager;
import at.ac.tuwien.dsg.pm.PeerManagerLauncher;
import at.ac.tuwien.dsg.rest.adapter.AdapterRestService;
import at.ac.tuwien.dsg.smartcom.GreenMailOutputAdapter;
import at.ac.tuwien.dsg.smartcom.SmartCom;
import at.ac.tuwien.dsg.smartcom.SmartComBuilder;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.utils.PropertiesLoader;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class Launcher {

    public static void main(String[] args) throws IOException, PeerAuthenticationException, CommunicationException {

        int mongodbPort = PropertiesLoader.getIntProperty("caas.properties", "mongodb.port");
        int pmPort = PropertiesLoader.getIntProperty("caas.properties", "pm.port");
        int mailboxPort = PropertiesLoader.getIntProperty("caas.properties", "mailbox.port");
        int smartcomApiPort = PropertiesLoader.getIntProperty("caas.properties", "smartcom.api.port");
        int smartcomMisPort = PropertiesLoader.getIntProperty("caas.properties", "smartcom.mis.port");
        int adapterRestPort = PropertiesLoader.getIntProperty("caas.properties", "adapter.rest.port");

        int port = PropertiesLoader.getIntProperty("caas.properties", "services.port");
        String uri = PropertiesLoader.getProperty("caas.properties", "services.uri");

        String mongoDBStorage = PropertiesLoader.getProperty("caas.properties", "mongodb.storage");
        String pmUri = PropertiesLoader.getProperty("caas.properties", "pm.uri");
        String mailboxUri = PropertiesLoader.getProperty("caas.properties", "mailbox.uri");
        String adapterRestUri = PropertiesLoader.getProperty("caas.properties", "adapter.rest.uri");

        MongoDBLauncher.MongoDBInstance mongodb = MongoDBLauncher.startMongoDB(mongodbPort, mongoDBStorage);

        PeerManager peerManager = PeerManagerLauncher.startPeerManager(pmPort, pmUri, mongodb.getClient(), false);
        PeerManagerConnector peerManagerConnector = new PeerManagerConnector("http://0.0.0.0:"+port+"/"+uri);
        PeerMailboxService mailboxService = PeerMailboxServiceLauncher.startPeerMailboxService(mailboxPort, mailboxUri, mongodb.getClient(), false);

        SmartCom smartCom = new SmartComBuilder(peerManagerConnector, peerManagerConnector, peerManagerConnector)
                .initAdapters(true)
                .initializeActiveMQ(true)
                .setMongoClient(mongodb.getClient())
                .setRestApiPort(smartcomApiPort)
                .setMessageInfoServicePort(smartcomMisPort)
                .useLocalMessageQueue(true)
                .create();
        System.out.println("Running the the SmartCom rest service on port ["+smartcomApiPort+"] and path 'SmartCom'");

        AdapterRestService adapterRestService = new AdapterRestService(adapterRestPort, adapterRestUri, smartCom.getCommunication());
        HttpServer server = startServer(peerManager, mailboxService, adapterRestService, port, uri);
        smartCom.getCommunication().removeOutputAdapter(Identifier.adapter("Email"));
        smartCom.getCommunication().registerOutputAdapter(GreenMailOutputAdapter.class);
        System.out.println("Press enter to shutdown the application");
        System.in.read();

        server.shutdownNow();
        adapterRestService.cleanUp();
        smartCom.tearDownSmartCom();
        mailboxService.cleanUp();
        peerManager.cleanUp();
        mongodb.tearDown();
    }

    public static HttpServer startServer(final PeerManager peerManager,
                                         final PeerMailboxService mailboxService,
                                         final AdapterRestService adapterRestService,
                                         int port, String uriPostfix) {
        URI serverURI = URI.create("http://0.0.0.0:" + port + "/" + uriPostfix);
        final ResourceConfig config = new ResourceConfig();

        Set<Class<?>> set = new HashSet<>();
        set.addAll(peerManager.getWebResources());
        set.addAll(mailboxService.getWebResources());
        set.addAll(adapterRestService.getWebResources());

        for (Class<?> aClass : set) {
            config.register(aClass);
        }

        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                peerManager.bindWebBindings(this);
                mailboxService.bindWebBindings(this);
                adapterRestService.bindWebBindings(this);
            }
        });

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(serverURI, config);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Could not initialize CommunicationRESTImpl: " + e.getLocalizedMessage());
        }

        return server;
    }
}
