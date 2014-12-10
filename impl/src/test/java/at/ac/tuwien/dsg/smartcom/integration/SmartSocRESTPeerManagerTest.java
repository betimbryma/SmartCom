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
package at.ac.tuwien.dsg.smartcom.integration;

import at.ac.tuwien.dsg.smartcom.adapters.FreePortProviderUtil;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.model.CollectiveInfo;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;
import at.ac.tuwien.dsg.smartcom.rest.ObjectMapperProvider;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertNotNull;

public class SmartSocRESTPeerManagerTest {

    private HttpServer server;
    private SmartSocRESTPeerManager manager;

    public static void main(String[] args) throws NoSuchCollectiveException, NoSuchPeerException, PeerAuthenticationException {
        SmartSocRESTPeerManager manager = new SmartSocRESTPeerManager(100);

        CollectiveInfo collectiveInfo = manager.getCollectiveInfo(Identifier.collective("5927"));
        System.out.println(collectiveInfo);

        Identifier peerId;
        if (collectiveInfo.getPeers().isEmpty()) {
            peerId = Identifier.peer("5910");
        } else {
            peerId = collectiveInfo.getPeers().get(0);
        }

        PeerInfo peerInfo = manager.getPeerInfo(peerId);
        System.out.println(peerInfo);

        boolean authenticate = manager.authenticate(Identifier.peer("1"), "1");
        System.out.println("Authentication: "+authenticate);
    }

//    @Before
    public void setUp() throws Exception {
        int freePort = FreePortProviderUtil.getFreePort();

        String url = "http://localhost:" + freePort + "/test/";
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(url), new RESTApplication());
        manager = new SmartSocRESTPeerManager(100, url+"collective", url+"peer",url+"peer",url+"peer", url+"authenticate");

        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @After
    public void tearDown() throws Exception {
        server.shutdownNow();
    }

//    @Test
    public void testCollective() throws Exception {
        CollectiveInfo collectiveInfo = manager.getCollectiveInfo(Identifier.collective("1"));
        assertNotNull(collectiveInfo);
    }

//    @Test
    public void testPeer() throws Exception {
        PeerInfo peerInfo = manager.getPeerInfo(Identifier.peer("1"));
        assertNotNull(peerInfo);
    }

    private class RESTApplication extends ResourceConfig {
        private RESTApplication() {
//            register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true));
            register(PeerManagerResource.class);
            register(ObjectMapperProvider.class);
            register(JacksonFeature.class);
        }
    }

}