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

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class JSONConverterTest {

    @Test
    public void testGetCollectiveInfo() throws Exception {
        String instance =
                "{" +
                    "\"@type\": \"EQLSearchResult\"," +
                    "\"results\": ["+
                        "["+
                            "\"1\","+
                            "\"2\""+
                        "]"+
                    "]"+
                "}";

        CollectiveInfo collectiveInfo = JSONConverter.getCollectiveInfo(Identifier.collective("1"), instance);

        assertEquals(Identifier.collective("1"), collectiveInfo.getId());
        assertEquals(DeliveryPolicy.Collective.TO_ALL_MEMBERS, collectiveInfo.getDeliveryPolicy());
        assertThat(collectiveInfo.getPeers(), Matchers.contains(Identifier.peer("1"), Identifier.peer("2")));
    }

//    @Test
    public void testAuthenticate() throws Exception {

    }

    @Test
    public void testGetPeerInfo() throws Exception {
        String instance =
            "{" +
                "\"@type\": \"EQLSearchResult\"," +
                "\"results\": [" +
                    "[" +
                        "\"5811\"," +
                        "\"5810\"," +
                        "\"5808\"," +
                        "\"1\"," +
                        "\"Email\"," +
                        "\"philipp.zeppezauer@gmail.com\"" +
                    "]," +
                    "[" +
                        "\"5811\"," +
                        "\"5810\"," +
                        "\"5809\"," +
                        "\"1\"," +
                        "\"Android\"," +
                        "\"AndroidID2\"" +
                    "]" +
                "]" +
            "}";

        Identifier peerId = Identifier.peer("1");
        PeerInfo peerInfo = JSONConverter.getPeerInfo(peerId, instance);

        PeerChannelAddress address1 = new PeerChannelAddress(peerId, Identifier.channelType("Email"), Arrays.asList("philipp.zeppezauer@gmail.com"));
        PeerChannelAddress address2 = new PeerChannelAddress(peerId, Identifier.channelType("Android"), Arrays.asList("AndroidID2"));

        assertEquals(peerId, peerInfo.getId());
        assertEquals(DeliveryPolicy.Peer.AT_LEAST_ONE, peerInfo.getDeliveryPolicy());
        assertThat(peerInfo.getAddresses(), Matchers.hasSize(2));
        assertThat(peerInfo.getAddresses(), Matchers.contains(address1, address2));

        instance =
            "{" +
                "\"@type\": \"EQLSearchResult\"," +
                "\"results\": [" +
                    "[" +
                        "\"5816\",\n" +
                        "\"5815\",\n" +
                        "\"5814\",\n" +
                        "\"0\",\n" +
                        "\"Android\",\n" +
                        "\"AndroidID3\"" +
                    "]," +
                    "[" +
                        "\"5816\",\n" +
                        "\"5815\",\n" +
                        "\"5814\",\n" +
                        "\"0\",\n" +
                        "\"Android\",\n" +
                        "\"dummyParameter\"" +
                    "]" +
                "]" +
            "}";

        peerId = Identifier.peer("1");
        peerInfo = JSONConverter.getPeerInfo(peerId, instance);

        PeerChannelAddress address = new PeerChannelAddress(peerId, Identifier.channelType("Android"), Arrays.asList("AndroidID3", "dummyParameter"));

        assertEquals(peerId, peerInfo.getId());
        assertEquals(DeliveryPolicy.Peer.TO_ALL_CHANNELS, peerInfo.getDeliveryPolicy());
        assertThat(peerInfo.getAddresses(), Matchers.hasSize(1));
        assertThat(peerInfo.getAddresses(), Matchers.contains(address));
    }
}