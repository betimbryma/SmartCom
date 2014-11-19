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

import at.ac.tuwien.dsg.smartcom.model.CollectiveInfo;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;
import at.ac.tuwien.dsg.smartcom.rest.ObjectMapperProvider;
import at.ac.tuwien.dsg.util.FreePortProviderUtil;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.*;

public class PeerManagerConnectorTest {

    private HttpServer server;
    private PeerManagerConnector connector;

    @Before
    public void setUp() throws Exception {
        int freePort = FreePortProviderUtil.getFreePort();

        URI serverURI = URI.create("http://localhost:" + freePort + "/test");
        server = GrizzlyHttpServerFactory.createHttpServer(serverURI, new RESTApplication());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        connector = new PeerManagerConnector("http://localhost:" + freePort + "/test");
    }

    @After
    public void tearDown() throws Exception {
        server.shutdownNow();
    }

    @Test
    public void testGetCollectiveInfo() throws Exception {
        CollectiveInfo collectiveInfo = connector.getCollectiveInfo(Identifier.collective("1"));
        assertNotNull(collectiveInfo);
        assertEquals(Identifier.collective("1"), collectiveInfo.getId());
        assertEquals(DeliveryPolicy.Collective.TO_ALL_MEMBERS, collectiveInfo.getDeliveryPolicy());
        assertThat(collectiveInfo.getPeers(), Matchers.contains(Identifier.peer("1"), Identifier.peer("2")));
    }

    @Test
    public void testAuthenticate() throws Exception {
        boolean authenticate = connector.authenticate(Identifier.peer("1"), "1");
        assertTrue(authenticate);
    }

    @Test
    public void testGetPeerInfo() throws Exception {
        PeerInfo peerInfo = connector.getPeerInfo(Identifier.peer("1"));
        assertNotNull(peerInfo);
        assertEquals(Identifier.peer("1"), peerInfo.getId());
        assertEquals(DeliveryPolicy.Peer.AT_LEAST_ONE, peerInfo.getDeliveryPolicy());
        assertEquals(null, peerInfo.getPrivacyPolicies());
        assertThat(peerInfo.getAddresses(), Matchers.hasSize(0));
    }


    private class RESTApplication extends ResourceConfig {
        private RESTApplication() {
            register(PeerManagerResource.class);

            register(MultiPartFeature.class);
            register(ObjectMapperProvider.class);
            register(JacksonFeature.class);
//            register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true));
        }
    }
}