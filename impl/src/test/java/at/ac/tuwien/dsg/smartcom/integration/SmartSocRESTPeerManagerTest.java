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

import at.ac.tuwien.dsg.smartcom.model.*;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SmartSocRESTPeerManagerTest {

    @Test
    public void testGetCollectiveInfo() throws Exception {
        String instance =
                "{" +
                    "\"name\": [\"test\", \"test1\", \"test3\"]," +
                    "\"owner\": {}," +
                    "\"members\": [" +
                        "{ " +
                            "\"username\": \"test1\"," +
                            "\"password\": \"test1\"," +
                            "\"peerId\": 1," +
                            "\"mainProfileDefinitionId\": 2," +
                            "\"defaultPolicies\": []," +
                            "\"profileDefinitions\": []," +
                            "\"id\": 1" +
                        "}," +
                        "{ " +
                            "\"username\": \"test2\"," +
                            "\"password\": \"test2\"," +
                            "\"peerId\": 2," +
                            "\"mainProfileDefinitionId\": 3," +
                            "\"defaultPolicies\": []," +
                            "\"profileDefinitions\": []," +
                            "\"id\": 2" +
                        "}," +
                    "]," +
                    "\"id\": 1" +
                "}";


        CollectiveInfo collectiveInfo = JSONConverter.getCollectiveInfo(Identifier.collective("1"), instance);

        assertEquals(Identifier.collective("1"), collectiveInfo.getId());
        assertEquals(DeliveryPolicy.Collective.TO_ALL_MEMBERS, collectiveInfo.getDeliveryPolicy());
        assertThat(collectiveInfo.getPeers(), Matchers.contains(Identifier.peer("1"), Identifier.peer("2")));
    }

    @Test
    public void testAuthenticate() throws Exception {

    }

    @Test
    public void testGetPeerInfo() throws Exception {
        String instance =
                "{" +
                    "\"definitionId\": 1," +
                    "\"ownerId\": 1," +
                    "\"name\": [\"peer1\", \"peer2\"]," +
                    "\"deliveryPolicy\": 0," +
                    "\"deliveryAddresses\": [" +
                        "{"+
                            "\"name\": \"email\"," +
                            "\"type\": \"email\"," +
                            "\"value\": [\"a.b@c.de\", \"c.b@a.de\"]"+
                        "},"+
                        "{"+
                            "\"name\": \"rest\"," +
                            "\"type\": \"rest\"," +
                            "\"value\": [\"http://localhost:8080/peer\"]"+
                        "}"+
                    "]," +
                    "\"agreedReqs\": null," +
                    "\"id\": 1"+
                "}";

        PeerInfo peerInfo = JSONConverter.getPeerInfo(Identifier.peer("1"), instance);

        assertEquals(Identifier.peer("1"), peerInfo.getId());
        assertEquals(DeliveryPolicy.Peer.TO_ALL_CHANNELS, peerInfo.getDeliveryPolicy());
        assertThat(peerInfo.getPrivacyPolicies(), Matchers.empty());
        assertThat(peerInfo.getAddresses(), Matchers.hasSize(2));
        for (PeerChannelAddress address : peerInfo.getAddresses()) {
            if (Identifier.channelType("email").equals(address.getChannelType())) {
                assertThat(address.getContactParameters(), Matchers.<Serializable>contains("a.b@c.de", "c.b@a.de"));
            } else {
                assertThat(address.getContactParameters(), Matchers.<Serializable>contains("http://localhost:8080/peer"));
            }
        }
    }
}