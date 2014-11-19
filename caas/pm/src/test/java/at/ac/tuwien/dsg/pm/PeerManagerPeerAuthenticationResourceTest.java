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
package at.ac.tuwien.dsg.pm;

import at.ac.tuwien.dsg.pm.dao.MongoDBCollectiveDAO;
import at.ac.tuwien.dsg.pm.dao.MongoDBPeerDAO;
import at.ac.tuwien.dsg.pm.model.Peer;
import at.ac.tuwien.dsg.pm.model.PeerAddress;
import at.ac.tuwien.dsg.pm.util.FreePortProviderUtil;
import at.ac.tuwien.dsg.pm.util.RequestMappingFeature;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.MongoClient;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PeerManagerPeerAuthenticationResourceTest {

    public String url = "http://localhost:8080/SmartCom/peerAuth";
    private MongoDBInstance mongoDB;

    private MongoDBPeerDAO peerDAO;

    private Client client;
    private PeerManager manager;

    @Before
    public void setUp() throws Exception {
        int mongoDbPort = FreePortProviderUtil.getFreePort();
        mongoDB = new MongoDBInstance(mongoDbPort);
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", mongoDbPort);
        peerDAO = new MongoDBPeerDAO(mongo, "TEST", "PEER");
        MongoDBCollectiveDAO collectiveDAO = new MongoDBCollectiveDAO(mongo, "TEST", "COLLECTIVE");

        this.client = ClientBuilder.newBuilder()
                .register(RequestMappingFeature.class)
                .property(ClientProperties.CONNECT_TIMEOUT, 5000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .build();
//        client.register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true)); //enables this to have additional logging information

        int freePort = FreePortProviderUtil.getFreePort();
        url = "http://localhost:"+freePort+"/SmartCom/peerAuth";
        manager = new PeerManager(freePort, "SmartCom", peerDAO, collectiveDAO);
        manager.init();
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
        client.close();
        manager.cleanUp();
    }

    @Test
    public void testPeerAuthentication() throws Exception {
        Peer peer1 = createAndAddPeer("1", "Peer1");
        Peer peer2 = createAndAddPeer("2", "Peer2");
        Peer peer3 = createAndAddPeer("3", "Peer3");
        Peer peer4 = createAndAddPeer("4", "Peer4");
        Peer peer5 = createAndAddPeer("5", "Peer5");

        List<Peer> peers = Arrays.asList(peer1, peer2, peer3, peer4, peer5);


        for (Peer peer : peers) {
            WebTarget target = client.target(url +"/"+peer.getId());
            Boolean auth = target.request(MediaType.APPLICATION_JSON).header("password", peer.getId()).get(Boolean.class);

            assertTrue(auth);
        }

        WebTarget target = client.target(url +"/1");
        Boolean auth = target.request(MediaType.APPLICATION_JSON).header("password", "2").get(Boolean.class);

        assertFalse(auth);
    }

    private Peer createAndAddPeer(String id, String name) {
        Peer peer = new Peer();
        peer.setId(id);
        peer.setName(name);
        peer.setDeliveryPolicy(DeliveryPolicy.Peer.AT_LEAST_ONE);

        PeerAddress address1 = new PeerAddress();
        address1.setType("email");
        address1.setValues(Arrays.asList("peer@peer.de"));

        PeerAddress address2 = new PeerAddress();
        address2.setType("skype");
        address2.setValues(Arrays.asList("peerSkype"));

        peer.setPeerAddressList(Arrays.asList(address1, address2));
        peerDAO.addPeer(peer);
        return peer;
    }
}