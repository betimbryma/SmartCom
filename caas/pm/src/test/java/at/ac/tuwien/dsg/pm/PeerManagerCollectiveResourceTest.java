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
import at.ac.tuwien.dsg.pm.model.Collective;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class PeerManagerCollectiveResourceTest {

    public String url = "http://localhost:8080/SmartCom/collective";
    private MongoDBInstance mongoDB;

    private MongoDBPeerDAO peerDAO;
    private MongoDBCollectiveDAO collectiveDAO;

    private Client client;
    private PeerManager manager;

    @Before
    public void setUp() throws Exception {
        int mongoDbPort = FreePortProviderUtil.getFreePort();
        mongoDB = new MongoDBInstance(mongoDbPort);
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", mongoDbPort);
        peerDAO = new MongoDBPeerDAO(mongo, "TEST", "PEER");
        collectiveDAO = new MongoDBCollectiveDAO(mongo, "TEST", "COLLECTIVE");

        this.client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
                .register(MultiPartFeature.class)
                .property(ClientProperties.CONNECT_TIMEOUT, 5000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .build();
//        client.register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true)); //enables this to have additional logging information

        int freePort = FreePortProviderUtil.getFreePort();
        url = "http://localhost:"+freePort+"/SmartCom/collective";
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
    public void testAddCollective() throws Exception {
        WebTarget target = client.target(url);

        Collective coll1 = createCollective("1");
        Collective coll2 = createCollective("2");
        Collective coll3 = createCollective("3");
        Collective coll4 = createCollective("4");
        Collective coll5 = createCollective("5");

        Collective response1 = target.request(MediaType.APPLICATION_JSON).post(Entity.json(coll1), Collective.class);
        assertEquals(coll1, response1);

        Collective response2 = target.request(MediaType.APPLICATION_JSON).post(Entity.json(coll2), Collective.class);
        assertEquals(coll2, response2);

        Collective response3 = target.request(MediaType.APPLICATION_JSON).post(Entity.json(coll3), Collective.class);
        assertEquals(coll3, response3);

        Collective response4 = target.request(MediaType.APPLICATION_JSON).post(Entity.json(coll4), Collective.class);
        assertEquals(coll4, response4);

        Collective response5 = target.request(MediaType.APPLICATION_JSON).post(Entity.json(coll5), Collective.class);
        assertEquals(coll5, response5);

        Collective coll6 = createCollective(null);
        Collective response6 = target.request(MediaType.APPLICATION_JSON).post(Entity.json(coll6), Collective.class);
        assertEquals(coll6.getDeliveryPolicy(), response6.getDeliveryPolicy());
        assertEquals(coll6.getPeers(), response6.getPeers());
        assertNotNull(response6.getId());
        assertThat(response6.getId(), Matchers.not(Matchers.isEmptyString()));

        List<Collective> collectives = Arrays.asList(coll1, coll2, coll3, coll4, coll5);

        for (Collective collective : collectives) {
            assertEquals(collective, collectiveDAO.getCollective(collective.getId()));
        }
    }

    @Test
    public void testGetCollective() throws Exception {
        Collective coll1 = createAndAddCollective("1");
        Collective coll2 = createAndAddCollective("2");
        Collective coll3 = createAndAddCollective("3");
        Collective coll4 = createAndAddCollective("4");
        Collective coll5 = createAndAddCollective("5");

        List<Collective> collectives = Arrays.asList(coll1, coll2, coll3, coll4, coll5);

        for (Collective collective : collectives) {
            WebTarget target = client.target(url +"/"+collective.getId());
            Collective response = target.request(MediaType.APPLICATION_JSON).get(Collective.class);
            assertEquals(collective, response);
        }
    }

    @Test
    public void testGetAll() throws Exception {
        Collective coll1 = createAndAddCollective("1");
        Collective coll2 = createAndAddCollective("2");
        Collective coll3 = createAndAddCollective("3");
        Collective coll4 = createAndAddCollective("4");
        Collective coll5 = createAndAddCollective("5");

        List<Collective> peers = Arrays.asList(coll1, coll2, coll3, coll4, coll5);

        WebTarget target = client.target(url + "/all");
        Collective[] response = target.request(MediaType.APPLICATION_JSON).get(Collective[].class);

        assertThat(Arrays.asList(response), Matchers.containsInAnyOrder(peers.toArray()));
    }

    @Test
    public void testUpdateCollective() throws Exception {
        Collective coll1 = createAndAddCollective("1");
        Collective coll2 = createAndAddCollective("2");
        Collective coll3 = createAndAddCollective("3");
        Collective coll4 = createAndAddCollective("4");
        Collective coll5 = createAndAddCollective("5");

        List<Collective> collectives = Arrays.asList(coll1, coll2, coll3, coll4, coll5);

        for (Collective collective : collectives) {
            collective.setDeliveryPolicy(DeliveryPolicy.Collective.TO_ANY);
            WebTarget target = client.target(url);
            Collective response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(collective), Collective.class);
            assertEquals(collective, response);
            assertEquals(collective, collectiveDAO.getCollective(collective.getId()));
        }
    }

    @Test
    public void testAddPeerToCollective() throws Exception {
        Collective coll1 = createAndAddCollective("1");
        Collective coll2 = createAndAddCollective("2");
        Collective coll3 = createAndAddCollective("3");
        Collective coll4 = createAndAddCollective("4");
        Collective coll5 = createAndAddCollective("5");

        Random randomGenerator = new Random(1234); //use seed to get the same results for every test run
        List<String> peers = Arrays.asList("1", "2", "3", "4", "5");
        List<Collective> collectives = Arrays.asList(coll1, coll2, coll3, coll4, coll5);

        for (Collective collective : collectives) {
            int times = randomGenerator.nextInt(peers.size());
            List<String> peersOfCollective = new ArrayList<>();
            for (int i = 0; i < times; i++) {
                String peer = peers.get(randomGenerator.nextInt(peers.size()));
                peersOfCollective.remove(peer);
                peersOfCollective.add(peer);

                WebTarget target = client.target(url + "/" + collective.getId() + "/" + peer);
                Collective response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""), Collective.class);

                assertThat(response.getPeers(), Matchers.containsInAnyOrder(peersOfCollective.toArray()));
            }

            Collective coll = collectiveDAO.getCollective(collective.getId());
            assertThat(coll.getPeers(), Matchers.containsInAnyOrder(peersOfCollective.toArray()));
        }
    }

    @Test
    public void testRemovePeerToCollective() throws Exception {
        Collective coll1 = createAndAddCollective("1");
        Collective coll2 = createAndAddCollective("2");
        Collective coll3 = createAndAddCollective("3");
        Collective coll4 = createAndAddCollective("4");
        Collective coll5 = createAndAddCollective("5");

        List<Collective> collectives = Arrays.asList(coll1, coll2, coll3, coll4, coll5);
        List<String> peers = Arrays.asList("1", "2", "3", "4", "5");

        for (Collective collective : collectives) {
            for (String peer : peers) {
                collectiveDAO.addPeerToCollective(collective.getId(), peer);
            }
        }

        coll1 = collectiveDAO.getCollective("1");
        coll2 = collectiveDAO.getCollective("2");
        coll3 = collectiveDAO.getCollective("3");
        coll4 = collectiveDAO.getCollective("4");
        coll5 = collectiveDAO.getCollective("5");
        collectives = Arrays.asList(coll1, coll2, coll3, coll4, coll5);

        Random randomGenerator = new Random(1234); //use seed to get the same results for every test run

        for (Collective collective : collectives) {
            int times = randomGenerator.nextInt(peers.size());
            List<String> peersOfCollective = new ArrayList<>();
            assertThat(collective.getPeers(), Matchers.containsInAnyOrder(peers.toArray()));

            for (int i = 0; i < times; i++) {
                String peer = peers.get(randomGenerator.nextInt(peers.size()));
                peersOfCollective.add(peer);

                WebTarget target = client.target(url + "/" + collective.getId() + "/" + peer);
                Collective response = target.request(MediaType.APPLICATION_JSON).delete(Collective.class);

                assertThat(response.getPeers(), Matchers.not(Matchers.containsInAnyOrder(peersOfCollective.toArray())));
            }

            Collective coll = collectiveDAO.getCollective(collective.getId());
            assertThat(coll.getPeers(), Matchers.not(Matchers.containsInAnyOrder(peersOfCollective.toArray())));
        }

    }

    @Test
    public void testDeleteCollective() throws Exception {


        Collective coll1 = createAndAddCollective("1");
        Collective coll2 = createAndAddCollective("2");
        Collective coll3 = createAndAddCollective("3");
        Collective coll4 = createAndAddCollective("4");
        Collective coll5 = createAndAddCollective("5");

        WebTarget target = client.target(url +"/"+coll3.getId());
        Collective response3 = target.request(MediaType.APPLICATION_JSON).get(Collective.class);
        assertEquals(coll3, response3);

        target = client.target(url +"/"+coll3.getId());
        Response delete = target.request(MediaType.APPLICATION_JSON).delete();

        assertEquals(Response.Status.OK.getStatusCode(), delete.getStatus());

        target = client.target(url +"/6");
        delete = target.request(MediaType.APPLICATION_JSON).delete();

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), delete.getStatus());

        target = client.target(url + "/all");
        Collective[] response = target.request(MediaType.APPLICATION_JSON).get(Collective[].class);

        List<Collective> collectives = Arrays.asList(coll1, coll2, coll4, coll5);
        assertThat(Arrays.asList(response), Matchers.not(Matchers.contains(coll3)));
        assertThat(Arrays.asList(response), Matchers.contains(collectives.toArray()));
    }

    @Test
    public void testDeleteAll() throws Exception {
        Collective coll1 = createAndAddCollective("1");
        Collective coll2 = createAndAddCollective("2");
        Collective coll3 = createAndAddCollective("3");
        Collective coll4 = createAndAddCollective("4");
        Collective coll5 = createAndAddCollective("5");

        List<Collective> peers = Arrays.asList(coll1, coll2, coll3, coll4, coll5);

        WebTarget target = client.target(url + "/all");
        Response delete = target.request(MediaType.APPLICATION_JSON).delete();
        assertEquals(Response.Status.OK.getStatusCode(), delete.getStatus());

        target = client.target(url + "/all");
        Collective[] response = target.request(MediaType.APPLICATION_JSON).get(Collective[].class);
        assertThat(Arrays.asList(response), Matchers.not(Matchers.containsInAnyOrder(peers.toArray())));
    }

    @Test
    public void testFileDownload() throws Exception {
        createAndAddCollective("1");
        createAndAddCollective("2");
        createAndAddCollective("3");
        createAndAddCollective("4");
        createAndAddCollective("5");

        WebTarget target = client.target(url +"/download");
        Response response = target.request(MediaType.APPLICATION_OCTET_STREAM).get();

        File file = response.readEntity(File.class);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            int lines = 0;
            while (reader.readLine() != null) {
                lines++;
            }
            assertEquals(5, lines);
        }
    }

    @Test
    public void testUploadFile() throws Exception {
        URL url = this.getClass().getResource("/test_collective_file.dump");
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
        assertThat(collectiveDAO.getAll(), Matchers.hasSize(4));
    }

    private Collective createAndAddCollective(String id) {
        return collectiveDAO.addCollective(createCollective(id));
    }

    private Collective createCollective(String id) {
        Collective coll = new Collective();
        coll.setId(id);
        coll.setDeliveryPolicy(DeliveryPolicy.Collective.TO_ALL_MEMBERS);
        return coll;
    }
}