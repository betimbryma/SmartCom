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
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.MongoClient;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PeerManagerPeerResourceTest {

    public String url = "http://localhost:8080/SmartCom/peer";
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
                .register(JacksonFeature.class)
                .register(MultiPartFeature.class)
                .property(ClientProperties.CONNECT_TIMEOUT, 5000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .build();
//        client.register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true)); //enables this to have additional logging information

        int freePort = FreePortProviderUtil.getFreePort();
        url = "http://localhost:"+freePort+"/SmartCom/peer";
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
    public void testAddPeer() throws Exception {
        WebTarget target = client.target(url);

        Peer peer1 = createPeer("1", "Peer1");
        Peer peer2 = createPeer("2", "Peer2");
        Peer peer3 = createPeer("3", "Peer3");
        Peer peer4 = createPeer("4", "Peer4");
        Peer peer5 = createPeer("5", "Peer5");

        Peer peer1Response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(peer1), Peer.class);
        assertEquals(peer1, peer1Response);

        Peer peer2Response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(peer2), Peer.class);
        assertEquals(peer2, peer2Response);

        Peer peer3Response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(peer3), Peer.class);
        assertEquals(peer3, peer3Response);

        Peer peer4Response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(peer4), Peer.class);
        assertEquals(peer4, peer4Response);

        Peer peer5Response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(peer5), Peer.class);
        assertEquals(peer5, peer5Response);

        Peer peer6 = createPeer(null, "Peer6");
        Peer peer6Response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(peer6), Peer.class);
        assertEquals(peer6.getDeliveryPolicy(), peer6Response.getDeliveryPolicy());
        assertEquals(peer6.getName(), peer6Response.getName());
        assertEquals(peer6.getPeerAddressList(), peer6Response.getPeerAddressList());
        assertNotNull(peer6Response.getId());
        assertThat(peer6Response.getId(), Matchers.not(Matchers.isEmptyString()));

        List<Peer> peers = Arrays.asList(peer1, peer2, peer3, peer4, peer5);

        for (Peer peer : peers) {
            assertEquals(peer, peerDAO.getPeer(peer.getId()));
        }

    }

    @Test
    public void testGetPeer() throws Exception {
        Peer peer1 = createPeer("1", "Peer1");
        Peer peer2 = createPeer("2", "Peer2");
        Peer peer3 = createPeer("3", "Peer3");
        Peer peer4 = createPeer("4", "Peer4");
        Peer peer5 = createPeer("5", "Peer5");

        peerDAO.addPeer(peer1);
        peerDAO.addPeer(peer2);
        peerDAO.addPeer(peer3);
        peerDAO.addPeer(peer4);
        peerDAO.addPeer(peer5);

        List<Peer> peers = Arrays.asList(peer1, peer2, peer3, peer4, peer5);

        for (Peer peer : peers) {
            WebTarget target = client.target(url +"/"+peer.getId());
            Peer response = target.request(MediaType.APPLICATION_JSON).get(Peer.class);
            assertEquals(peer, response);
        }
    }

    @Test
    public void testGetAll() throws Exception {
        Peer peer1 = createPeer("1", "Peer1");
        Peer peer2 = createPeer("2", "Peer2");
        Peer peer3 = createPeer("3", "Peer3");
        Peer peer4 = createPeer("4", "Peer4");
        Peer peer5 = createPeer("5", "Peer5");

        peerDAO.addPeer(peer1);
        peerDAO.addPeer(peer2);
        peerDAO.addPeer(peer3);
        peerDAO.addPeer(peer4);
        peerDAO.addPeer(peer5);

        List<Peer> peers = Arrays.asList(peer1, peer2, peer3, peer4, peer5);

        WebTarget target = client.target(url + "/all");
        Peer[] response = target.request(MediaType.APPLICATION_JSON).get(Peer[].class);

        assertThat(Arrays.asList(response), Matchers.containsInAnyOrder(peers.toArray()));
    }

    @Test
    public void testUpdatePeer() throws Exception {
        Peer peer1 = createPeer("1", "Peer1");
        Peer peer2 = createPeer("2", "Peer2");
        Peer peer3 = createPeer("3", "Peer3");
        Peer peer4 = createPeer("4", "Peer4");
        Peer peer5 = createPeer("5", "Peer5");

        peerDAO.addPeer(peer1);
        peerDAO.addPeer(peer2);
        peerDAO.addPeer(peer3);
        peerDAO.addPeer(peer4);
        peerDAO.addPeer(peer5);

        List<Peer> peers = Arrays.asList(peer1, peer2, peer3, peer4, peer5);

        for (Peer peer : peers) {
            peer.setName("changedName"+peer.getId());
            WebTarget target = client.target(url);
            Peer response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(peer), Peer.class);
            assertEquals(peer, response);

            assertEquals(response, peerDAO.getPeer(peer.getId()));
        }
    }

    @Test
    public void testDeletePeer() throws Exception {
        Peer peer1 = createPeer("1", "Peer1");
        Peer peer2 = createPeer("2", "Peer2");
        Peer peer3 = createPeer("3", "Peer3");
        Peer peer4 = createPeer("4", "Peer4");
        Peer peer5 = createPeer("5", "Peer5");

        peerDAO.addPeer(peer1);
        peerDAO.addPeer(peer2);
        peerDAO.addPeer(peer3);
        peerDAO.addPeer(peer4);
        peerDAO.addPeer(peer5);

        WebTarget target = client.target(url +"/"+peer3.getId());
        Response delete = target.request(MediaType.APPLICATION_JSON).delete();

        assertEquals(Response.Status.OK.getStatusCode(), delete.getStatus());

        target = client.target(url +"/Peer6");
        delete = target.request(MediaType.APPLICATION_JSON).delete();

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), delete.getStatus());

        target = client.target(url + "/all");
        Peer[] response = target.request(MediaType.APPLICATION_JSON).get(Peer[].class);

        assertThat(Arrays.asList(response), Matchers.not(Matchers.contains(peer3)));
    }

    @Test
    public void testDeleteAllPeers() throws Exception {
        Peer peer1 = createPeer("1", "Peer1");
        Peer peer2 = createPeer("2", "Peer2");
        Peer peer3 = createPeer("3", "Peer3");
        Peer peer4 = createPeer("4", "Peer4");
        Peer peer5 = createPeer("5", "Peer5");

        peerDAO.addPeer(peer1);
        peerDAO.addPeer(peer2);
        peerDAO.addPeer(peer3);
        peerDAO.addPeer(peer4);
        peerDAO.addPeer(peer5);

        WebTarget target = client.target(url +"/all");
        Response delete = target.request(MediaType.APPLICATION_JSON).delete();

        assertEquals(Response.Status.OK.getStatusCode(), delete.getStatus());

        List<Peer> peers = Arrays.asList(peer1, peer2, peer3, peer4, peer5);

        target = client.target(url + "/all");
        Peer[] response = target.request(MediaType.APPLICATION_JSON).get(Peer[].class);

        assertThat(Arrays.asList(response), Matchers.not(Matchers.contains(peers.toArray())));
    }

    @Test
    public void testFileDownload() throws Exception {
        Peer peer1 = createPeer("1", "Peer1");
        Peer peer2 = createPeer("2", "Peer2");
        Peer peer3 = createPeer("3", "Peer3");
        Peer peer4 = createPeer("4", "Peer4");
        Peer peer5 = createPeer("5", "Peer5");

        peerDAO.addPeer(peer1);
        peerDAO.addPeer(peer2);
        peerDAO.addPeer(peer3);
        peerDAO.addPeer(peer4);
        peerDAO.addPeer(peer5);

        WebTarget target = client.target(url +"/download");
        Response response = target.request(MediaType.APPLICATION_OCTET_STREAM).get();

        File file = response.readEntity(File.class);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            assertEquals(5, reader.lines().count());
        }
    }

    @Test
    public void testFileUpload() throws Exception {
        URL url = this.getClass().getResource("/test_peer_file.dump");
        File file = new File(url.toURI());

        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        if (file != null) {
            multiPart.bodyPart(new FileDataBodyPart("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        } else {
            fail("Could not load file");
        }

        WebTarget target = client.target(this.url + "/upload");
        target.request(MediaType.MULTIPART_FORM_DATA_TYPE).post(Entity.entity(multiPart, multiPart.getMediaType()));

        assertThat(peerDAO.getAll(), Matchers.hasSize(5));
    }




    private Peer createPeer(String id, String name) {
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
        return peer;
    }
}