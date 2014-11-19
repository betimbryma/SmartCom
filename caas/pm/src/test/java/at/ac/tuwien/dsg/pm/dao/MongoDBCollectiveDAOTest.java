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

import at.ac.tuwien.dsg.pm.exceptions.CollectiveAlreadyExistsException;
import at.ac.tuwien.dsg.pm.model.Collective;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.MongoClient;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MongoDBCollectiveDAOTest {

    private MongoDBInstance mongoDB;

    private MongoDBCollectiveDAO dao;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", 12345);
        dao = new MongoDBCollectiveDAO(mongo, "TEST", "PEER");
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
    }

    @Test
    public void testAddCollective() throws Exception {
        Collective coll1 = createCollective("coll1");
        Collective coll2 = createCollective("coll2");
        Collective coll3 = createCollective("coll3");
        Collective coll4 = createCollective("coll4");
        Collective coll5 = createCollective("coll5");

        coll1 = dao.addCollective(coll1);
        coll2 = dao.addCollective(coll2);
        coll3 = dao.addCollective(coll3);
        coll4 = dao.addCollective(coll4);
        coll5 = dao.addCollective(coll5);

        assertEquals(coll1, dao.getCollective("coll1"));
        assertEquals(coll2, dao.getCollective("coll2"));
        assertEquals(coll3, dao.getCollective("coll3"));
        assertEquals(coll4, dao.getCollective("coll4"));
        assertEquals(coll5, dao.getCollective("coll5"));
    }

    @Test(expected = CollectiveAlreadyExistsException.class)
    public void testAddCollective_duplicateKey() throws Exception {
        Collective coll1 = createCollective("coll1");
        Collective coll2 = createCollective("coll2");
        Collective coll3 = createCollective("coll3");
        Collective coll4 = createCollective("coll4");
        Collective coll5 = createCollective("coll5");

        coll1 = dao.addCollective(coll1);
        coll2 = dao.addCollective(coll2);
        coll3 = dao.addCollective(coll3);
        coll4 = dao.addCollective(coll4);
        coll5 = dao.addCollective(coll5);

        assertEquals(coll1, dao.getCollective("coll1"));
        assertEquals(coll2, dao.getCollective("coll2"));
        assertEquals(coll3, dao.getCollective("coll3"));
        assertEquals(coll4, dao.getCollective("coll4"));
        assertEquals(coll5, dao.getCollective("coll5"));

        dao.addCollective(createCollective("coll3"));
    }

    @Test
    public void testAddCollective_keyGeneration() throws Exception {
        Collective coll1 = createCollective("coll1");
        Collective coll2 = createCollective("coll2");
        Collective coll3 = createCollective("coll3");
        Collective coll4 = createCollective("coll4");
        Collective coll5 = createCollective("coll5");

        coll1 = dao.addCollective(coll1);
        coll2 = dao.addCollective(coll2);
        coll3 = dao.addCollective(coll3);
        coll4 = dao.addCollective(coll4);
        coll5 = dao.addCollective(coll5);

        assertEquals(coll1, dao.getCollective("coll1"));
        assertEquals(coll2, dao.getCollective("coll2"));
        assertEquals(coll3, dao.getCollective("coll3"));
        assertEquals(coll4, dao.getCollective("coll4"));
        assertEquals(coll5, dao.getCollective("coll5"));

        Collective coll = new Collective();
        coll.setDeliveryPolicy(DeliveryPolicy.Collective.TO_ANY);

        assertNull(coll.getId());
        coll = dao.addCollective(coll);
        assertNotNull(coll.getId());
        assertThat(coll.getId(), Matchers.not(Matchers.isEmptyString()));
    }

    @Test
    public void testGetCollective() throws Exception {
        assertNull(dao.getCollective("coll1"));

        Collective coll1 = createCollective("coll1");
        coll1 = dao.addCollective(coll1);

        assertEquals(coll1, dao.getCollective("coll1"));
    }

    @Test
    public void testGetAll() throws Exception {
        Collective coll1 = createAndAddCollective("coll1");
        Collective coll2 = createAndAddCollective("coll2");
        Collective coll3 = createAndAddCollective("coll3");
        Collective coll4 = createAndAddCollective("coll4");
        Collective coll5 = createAndAddCollective("coll5");

        List<Collective> all = dao.getAll();
        assertThat(all, Matchers.contains(coll1, coll2, coll3, coll4, coll5));
    }

    @Test
    public void testUpdateCollective() throws Exception {
        Collective coll1 = createAndAddCollective("coll1");
        Collective coll2 = createAndAddCollective("coll2");
        Collective coll3 = createAndAddCollective("coll3");
        Collective coll4 = createAndAddCollective("coll4");
        Collective coll5 = createAndAddCollective("coll5");

        coll3.setDeliveryPolicy(DeliveryPolicy.Collective.TO_ANY);
        coll3 = dao.updateCollective(coll3);
        assertEquals(DeliveryPolicy.Collective.TO_ANY, coll3.getDeliveryPolicy());

        Collective actual = dao.getCollective(coll3.getId());
        assertEquals(coll3, actual);

        coll4.addPeer("asdf");
        coll4 = dao.updateCollective(coll3);
        assertThat(coll4.getPeers(), Matchers.hasSize(0));

        actual = dao.getCollective(coll3.getId());
        assertEquals(coll4, actual);
    }

    @Test
    public void testAddPeerToCollective() throws Exception {
        Collective coll1 = createAndAddCollective("coll1");
        Collective coll2 = createAndAddCollective("coll2");
        Collective coll3 = createAndAddCollective("coll3");
        Collective coll4 = createAndAddCollective("coll4");
        Collective coll5 = createAndAddCollective("coll5");

        Collective actual = dao.addPeerToCollective(coll1.getId(), "peer1");
        assertThat(actual.getPeers(), Matchers.contains("peer1"));

        actual = dao.addPeerToCollective(coll1.getId(), "peer2");
        assertThat(actual.getPeers(), Matchers.contains("peer1", "peer2"));

        actual = dao.addPeerToCollective(coll1.getId(), "peer2");
        assertThat(actual.getPeers(), Matchers.hasSize(2));
        assertThat(actual.getPeers(), Matchers.contains("peer1", "peer2"));

        Collective actual2 = dao.addPeerToCollective(coll2.getId(), "peer2");
        assertThat(actual2.getPeers(), Matchers.hasSize(1));
        assertThat(actual2.getPeers(), Matchers.contains("peer2"));

        actual = dao.getCollective("coll1");
        assertThat(actual.getPeers(), Matchers.hasSize(2));
        assertThat(actual.getPeers(), Matchers.contains("peer1", "peer2"));
    }

    @Test
    public void testRemovePeerToCollective() throws Exception {
        Collective coll1 = createAndAddCollective("coll1");
        Collective coll2 = createAndAddCollective("coll2");
        Collective coll3 = createAndAddCollective("coll3");
        Collective coll4 = createAndAddCollective("coll4");
        Collective coll5 = createAndAddCollective("coll5");

        Collective actual = dao.addPeerToCollective(coll1.getId(), "peer1");
        assertThat(actual.getPeers(), Matchers.contains("peer1"));

        actual = dao.addPeerToCollective(coll1.getId(), "peer2");
        assertThat(actual.getPeers(), Matchers.contains("peer1", "peer2"));

        actual = dao.removePeerToCollective(coll1.getId(), "peer3");
        assertThat(actual.getPeers(), Matchers.contains("peer1", "peer2"));

        actual = dao.removePeerToCollective(coll1.getId(), "peer1");
        assertThat(actual.getPeers(), Matchers.contains("peer2"));
        assertThat(actual.getPeers(), Matchers.not(Matchers.contains("peer1")));

        actual = dao.getCollective(coll1.getId());
        assertThat(actual.getPeers(), Matchers.contains("peer2"));
        assertThat(actual.getPeers(), Matchers.not(Matchers.contains("peer1")));

        actual = dao.removePeerToCollective(coll1.getId(), "peer2");
        assertThat(actual.getPeers(), Matchers.not(Matchers.contains("peer2")));

        actual = dao.removePeerToCollective(coll1.getId(), "peer3");
        assertThat(actual.getPeers(), Matchers.not(Matchers.contains("peer2")));
    }

    @Test
    public void testDeleteCollective() throws Exception {
        Collective coll1 = createAndAddCollective("coll1");
        Collective coll2 = createAndAddCollective("coll2");
        Collective coll3 = createAndAddCollective("coll3");
        Collective coll4 = createAndAddCollective("coll4");
        Collective coll5 = createAndAddCollective("coll5");

        assertNotNull(dao.getCollective(coll2.getId()));

        dao.deleteCollective(coll2.getId());
        assertNull(dao.getCollective(coll2.getId()));
    }

    @Test
    public void testClearData() throws Exception {
        Collective coll1 = createAndAddCollective("coll1");
        Collective coll2 = createAndAddCollective("coll2");
        Collective coll3 = createAndAddCollective("coll3");
        Collective coll4 = createAndAddCollective("coll4");
        Collective coll5 = createAndAddCollective("coll5");

        assertThat(dao.getAll(), Matchers.contains(coll1, coll2, coll3, coll4, coll5));

        dao.clearData();
        assertThat(dao.getAll(), Matchers.empty());
    }

    private Collective createAndAddCollective(String id) throws CollectiveAlreadyExistsException {
        return dao.addCollective(createCollective(id));
    }

    private Collective createCollective(String id) {
        Collective coll = new Collective();
        coll.setId(id);
        coll.setDeliveryPolicy(DeliveryPolicy.Collective.TO_ALL_MEMBERS);
        return coll;
    }
}