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
package at.ac.tuwien.dsg.pm.dao;

import at.ac.tuwien.dsg.pm.exceptions.PeerAlreadyExistsException;
import at.ac.tuwien.dsg.pm.model.Peer;
import at.ac.tuwien.dsg.pm.model.PeerAddress;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.MongoClient;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class MongoDBPeerDAOTest {

    private MongoDBInstance mongoDB;

    private MongoDBPeerDAO dao;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", 12345);
        dao = new MongoDBPeerDAO(mongo, "TEST", "PEER");
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
    }

    @Test
    public void testAddAndGetPeer() throws Exception {
        Peer peer = createPeer("1", "TestPeer1");
        dao.addPeer(peer);

        Peer returnedPeer = dao.getPeer(peer.getId());

        assertEquals(peer, returnedPeer);
    }

    @Test
    public void testAddAndGetPeer_keyGeneration() throws Exception {
        Peer peer = createPeer("1", "TestPeer1");
        dao.addPeer(peer);

        Peer returnedPeer = dao.getPeer(peer.getId());
        assertEquals(peer, returnedPeer);

        Peer peer2 = createPeer(null, "TestPeer2");
        assertNull(peer2.getId());
        peer2 = dao.addPeer(peer2);
        assertNotNull(peer2.getId());
        assertThat(peer2.getId(), Matchers.not(Matchers.isEmptyString()));
    }

    @Test(expected = PeerAlreadyExistsException.class)
    public void testAddAndGetPeer_duplicateKey() throws Exception {
        Peer peer = createPeer("1", "TestPeer1");
        dao.addPeer(peer);

        Peer returnedPeer = dao.getPeer(peer.getId());
        assertEquals(peer, returnedPeer);

        Peer peer2 = createPeer("1", "TestPeer1");
        dao.addPeer(peer2);
    }

    @Test
    public void testGetAll() throws Exception {
        Peer peer1 = createAndAddPeer("1", "TestPeer1");
        Peer peer2 = createAndAddPeer("2", "TestPeer2");
        Peer peer3 = createAndAddPeer("3", "TestPeer3");
        Peer peer4 = createAndAddPeer("4", "TestPeer4");
        Peer peer5 = createAndAddPeer("5", "TestPeer5");

        List<Peer> all = dao.getAll();
        assertThat(all, Matchers.contains(peer1, peer2, peer3, peer4, peer5));
    }

    @Test
    public void testUpdatePeer() throws Exception {
        Peer peer = createPeer("1", "TestPeer1");
        dao.addPeer(peer);

        Peer returnedPeer = dao.getPeer("1");
        assertEquals(peer, returnedPeer);
        assertEquals("TestPeer1", returnedPeer.getName());

        peer.setName("TestPeer2");
        dao.updatePeer(peer);

        returnedPeer = dao.getPeer("1");
        assertEquals(peer, returnedPeer);
        assertEquals("TestPeer2", returnedPeer.getName());
    }

    @Test
    public void testDeletePeer() throws Exception {
        Peer peer = createPeer("1", "TestPeer1");
        dao.addPeer(peer);

        Peer peer2 = createPeer("2", "TestPeer2");
        dao.addPeer(peer2);

        Peer peer3 = createPeer("3", "TestPeer3");
        dao.addPeer(peer3);

        assertNotNull(dao.getPeer("1"));
        assertNotNull(dao.getPeer("2"));
        assertNotNull(dao.getPeer("3"));

        dao.deletePeer("2");
        assertNotNull(dao.getPeer("1"));
        assertNull(dao.getPeer("2"));
        assertNotNull(dao.getPeer("3"));
    }

    @Test
    public void testClearData() throws Exception {
        Peer peer1 = createAndAddPeer("1", "TestPeer1");
        Peer peer2 = createAndAddPeer("2", "TestPeer2");
        Peer peer3 = createAndAddPeer("3", "TestPeer3");
        Peer peer4 = createAndAddPeer("4", "TestPeer4");
        Peer peer5 = createAndAddPeer("5", "TestPeer5");

        assertThat(dao.getAll(), Matchers.contains(peer1, peer2, peer3, peer4, peer5));

        dao.clearData();
        assertThat(dao.getAll(), Matchers.empty());
    }

    private Peer createAndAddPeer(String id, String name) throws PeerAlreadyExistsException {
        return dao.addPeer(createPeer(id, name));
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