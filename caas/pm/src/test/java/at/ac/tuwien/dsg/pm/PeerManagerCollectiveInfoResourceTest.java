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
import at.ac.tuwien.dsg.pm.util.RequestMappingFeature;
import at.ac.tuwien.dsg.smartcom.model.CollectiveInfo;
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
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PeerManagerCollectiveInfoResourceTest {

    public String url = "http://localhost:8080/SmartCom/collectiveInfo";
    private MongoDBInstance mongoDB;

    private MongoDBPeerDAO peerDAO;

    private Client client;
    private PeerManager manager;
    private MongoDBCollectiveDAO collectiveDAO;

    @Before
    public void setUp() throws Exception {
        int mongoDbPort = FreePortProviderUtil.getFreePort();
        mongoDB = new MongoDBInstance(mongoDbPort);
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", mongoDbPort);
        peerDAO = new MongoDBPeerDAO(mongo, "TEST", "PEER");
        collectiveDAO = new MongoDBCollectiveDAO(mongo, "TEST", "COLLECTIVE");

        this.client = ClientBuilder.newBuilder()
                .register(RequestMappingFeature.class)
                .property(ClientProperties.CONNECT_TIMEOUT, 5000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .build();
//        client.register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true)); //enables this to have additional logging information

        int freePort = FreePortProviderUtil.getFreePort();
        url = "http://localhost:"+freePort+"/SmartCom/collectiveInfo";
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
    public void testCollectiveInfo() throws Exception {
        Collective coll1 = createAndAddCollective("1");
        Collective coll2 = createAndAddCollective("2");
        Collective coll3 = createAndAddCollective("3");
        Collective coll4 = createAndAddCollective("4");
        Collective coll5 = createAndAddCollective("5");

        List<Collective> collectives = Arrays.asList(coll1, coll2, coll3, coll4, coll5);


        for (Collective collective : collectives) {
            WebTarget target = client.target(url +"/"+collective.getId());
            CollectiveInfo info = target.request(MediaType.APPLICATION_JSON).get(CollectiveInfo.class);

            assertEquals(collective.getId(), info.getId().getId());
            assertEquals(collective.getDeliveryPolicy(), info.getDeliveryPolicy());
            assertEquals(collective.getPeers().size(), info.getPeers().size());
        }

        WebTarget target = client.target(url +"/6");
        Response response = target.request(MediaType.APPLICATION_JSON).get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
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