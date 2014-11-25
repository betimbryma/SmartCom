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

import at.ac.tuwien.dsg.smartcom.model.CollectiveInfo;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import org.hamcrest.Matchers;
import org.junit.Test;

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
                    "\"results\": ["+
                        "["+
                            "\"Mary\","+
                            "\"true\","+
                            "\"Monday\","+
                            "\"6604\","+
                            "\"0\""+
                        "]"+
                    "]"+
                "}";

        Identifier peerId = Identifier.peer("1");
        String[] strings = JSONConverter.parsePeerInfo(peerId, instance);

        assertThat(strings, Matchers.hasItemInArray("Mary"));
        assertThat(strings, Matchers.hasItemInArray("true"));
        assertThat(strings, Matchers.hasItemInArray("Monday"));
        assertThat(strings, Matchers.hasItemInArray("6604"));
        assertThat(strings, Matchers.hasItemInArray("0"));

        instance =
                "{" +
                    "\"@type\": \"EQLSearchResult\","+
                    "\"results\": ["+
                        "["+
                            "\"3323\","+
                            "\"3324\""+
                        "]"+
                    "]"+
                "}";

        strings = JSONConverter.parseUserInfo(peerId, instance);
        assertThat(strings, Matchers.hasItemInArray("3323"));
        assertThat(strings, Matchers.hasItemInArray("3324"));

        instance =
                "{" +
                    "\"@type\": \"EQLSearchResult\"," +
                    "\"results\": [" +
                        "[" +
                            "\"6005\"," +
                            "\"Android\"," +
                            "\"AndroidID31245\"" +
                        "]" +
                    "]" +
                "}";

        PeerChannelAddress address1 = JSONConverter.parsePeerAddressInfo(peerId, instance);

        assertEquals(peerId, address1.getPeerId());
        assertEquals(Identifier.channelType("Android"), address1.getChannelType());
        assertThat(address1.getContactParameters(), Matchers.hasSize(1));
    }
}