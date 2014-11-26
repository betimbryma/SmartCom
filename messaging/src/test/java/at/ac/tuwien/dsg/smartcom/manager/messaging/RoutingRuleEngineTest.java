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
package at.ac.tuwien.dsg.smartcom.manager.messaging;

import at.ac.tuwien.dsg.smartcom.exception.InvalidRuleException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class RoutingRuleEngineTest {

    RoutingRuleEngine engine;

    @Before
    public void setUp() throws Exception {
        engine = new RoutingRuleEngine();
    }

    @After
    public void tearDown() throws Exception {
        engine.clear();
    }

    @Test(expected = InvalidRuleException.class)
    public void testAddRouting_error1() throws Exception {
        RoutingRule rule = new RoutingRule("type", "subtype", Identifier.peer("peer"), null);
        engine.addRouting(rule);
    }

    @Test(expected = InvalidRuleException.class)
    public void testAddRouting_error2() throws Exception {
        RoutingRule rule = new RoutingRule("", "", Identifier.peer(null), Identifier.peer("receiver"));
        engine.addRouting(rule);
    }

    @Test
    public void testAddRouting_nullValues() throws Exception {
        RoutingRule rule1 = new RoutingRule("type", "subtype", Identifier.peer("peer"), Identifier.peer("receiver"));
        engine.addRouting(rule1);
        assertEquals(1, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertNotNull(engine.routing.get("type").get("subtype"));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")));

        RoutingRule rule2 = new RoutingRule(null, "subtype", Identifier.peer("peer"), Identifier.peer("receiver"));
        engine.addRouting(rule2);
        assertEquals(2, engine.routing.size());
        assertNotNull(engine.routing.get(null));
        assertNotNull(engine.routing.get(null).get("subtype"));
        assertNotNull(engine.routing.get(null).get("subtype").get(Identifier.peer("peer")));

        RoutingRule rule3 = new RoutingRule("type", null, Identifier.peer("peer"), Identifier.peer("receiver"));
        engine.addRouting(rule3);
        assertEquals(2, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertNotNull(engine.routing.get("type").get(null));
        assertNotNull(engine.routing.get("type").get(null).get(Identifier.peer("peer")));

        RoutingRule rule4 = new RoutingRule("type", "subtype", null, Identifier.peer("receiver"));
        engine.addRouting(rule4);
        assertEquals(2, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertNotNull(engine.routing.get("type").get("subtype"));
        assertNotNull(engine.routing.get("type").get("subtype").get(null));

        RoutingRule rule5 = new RoutingRule(null, "subtype", null, Identifier.peer("receiver"));
        engine.addRouting(rule5);
        assertEquals(2, engine.routing.size());
        assertNotNull(engine.routing.get(null));
        assertNotNull(engine.routing.get(null).get("subtype"));
        assertNotNull(engine.routing.get(null).get("subtype").get(null));

        RoutingRule rule6 = new RoutingRule(null, null, Identifier.peer("peer"), Identifier.peer("receiver"));
        engine.addRouting(rule6);
        assertEquals(2, engine.routing.size());
        assertNotNull(engine.routing.get(null));
        assertNotNull(engine.routing.get(null).get(null));
        assertNotNull(engine.routing.get(null).get(null).get(Identifier.peer("peer")));

        RoutingRule rule7 = new RoutingRule("type", null, null, Identifier.peer("receiver"));
        engine.addRouting(rule7);
        assertEquals(2, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertNotNull(engine.routing.get("type").get(null));
        assertNotNull(engine.routing.get("type").get(null).get(null));
    }

    @Test
    public void testAddRouting_multiType() throws Exception {
        createAndAddRoute("type1", "subtype", Identifier.peer("peer"), Identifier.peer("receiver"));
        createAndAddRoute("type2", "subtype", Identifier.peer("peer"), Identifier.peer("receiver"));
        createAndAddRoute("type3", "subtype", Identifier.peer("peer"), Identifier.peer("receiver"));
        createAndAddRoute("type4", "subtype", Identifier.peer("peer"), Identifier.peer("receiver"));

        assertEquals(4, engine.routing.size());
        assertNotNull(engine.routing.get("type1"));
        assertNotNull(engine.routing.get("type2"));
        assertNotNull(engine.routing.get("type3"));
        assertNotNull(engine.routing.get("type4"));

        createAndAddRoute("type", "subtype1", Identifier.peer("peer"), Identifier.peer("receiver"));
        createAndAddRoute("type", "subtype2", Identifier.peer("peer"), Identifier.peer("receiver"));
        createAndAddRoute("type", "subtype3", Identifier.peer("peer"), Identifier.peer("receiver"));
        createAndAddRoute("type", "subtype4", Identifier.peer("peer"), Identifier.peer("receiver"));

        assertEquals(5, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertEquals(4, engine.routing.get("type").size());
        assertNotNull(engine.routing.get("type").get("subtype1"));
        assertNotNull(engine.routing.get("type").get("subtype2"));
        assertNotNull(engine.routing.get("type").get("subtype3"));
        assertNotNull(engine.routing.get("type").get("subtype4"));

        createAndAddRoute("type", "subtype", Identifier.peer("peer1"), Identifier.peer("receiver"));
        createAndAddRoute("type", "subtype", Identifier.peer("peer2"), Identifier.peer("receiver"));
        createAndAddRoute("type", "subtype", Identifier.peer("peer3"), Identifier.peer("receiver"));
        createAndAddRoute("type", "subtype", Identifier.peer("peer4"), Identifier.peer("receiver"));

        assertEquals(5, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertEquals(5, engine.routing.get("type").size());
        assertNotNull(engine.routing.get("type").get("subtype").size());
        assertEquals(4, engine.routing.get("type").get("subtype").size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer1")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer2")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer3")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer4")));

        Identifier id1 = createAndAddRoute("type", "subtype", Identifier.peer("peer"), Identifier.peer("receiver1"));
        Identifier id2 = createAndAddRoute("type", "subtype", Identifier.peer("peer"), Identifier.peer("receiver2"));
        Identifier id3 = createAndAddRoute("type", "subtype", Identifier.peer("peer"), Identifier.peer("receiver3"));
        Identifier id4 = createAndAddRoute("type", "subtype", Identifier.peer("peer"), Identifier.peer("receiver3"));

        assertEquals(5, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertEquals(5, engine.routing.get("type").size());
        assertNotNull(engine.routing.get("type").get("subtype"));
        assertEquals(5, engine.routing.get("type").get("subtype").size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")));
        assertEquals(4, engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(id1));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(id2));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(id3));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(id4));
    }

    @Test
    public void testPerformRouting_empty() throws Exception {
        assertThat(engine.performRouting(createMessage("type", "subtype", "peer")), Matchers.hasSize(0));
    }

    @Test
    public void testPerformRouting() throws Exception {
        createAndAddRoute("single", "single", Identifier.peer("single"), Identifier.peer("single"));

        createAndAddRoute("type1", "single", Identifier.peer("single"), Identifier.peer("single"));
        createAndAddRoute("type2", "single", Identifier.peer("single"), Identifier.peer("single"));
        createAndAddRoute("type3", "single", Identifier.peer("single"), Identifier.peer("single"));
        createAndAddRoute("type4", "single", Identifier.peer("single"), Identifier.peer("single"));

        createAndAddRoute("multi", "subtype1", Identifier.peer("single"), Identifier.peer("single"));
        createAndAddRoute("multi", "subtype2", Identifier.peer("single"), Identifier.peer("single"));
        createAndAddRoute("multi", "subtype3", Identifier.peer("single"), Identifier.peer("single"));
        createAndAddRoute("multi", "subtype4", Identifier.peer("single"), Identifier.peer("single"));

        createAndAddRoute("multi", "multi", Identifier.peer("peer1"), Identifier.peer("single"));
        createAndAddRoute("multi", "multi", Identifier.peer("peer2"), Identifier.peer("single"));
        createAndAddRoute("multi", "multi", Identifier.peer("peer3"), Identifier.peer("single"));
        createAndAddRoute("multi", "multi", Identifier.peer("peer4"), Identifier.peer("single"));

        createAndAddRoute("multi", "multi", Identifier.peer("multi"), Identifier.peer("receiver1"));
        createAndAddRoute("multi", "multi", Identifier.peer("multi"), Identifier.peer("receiver2"));
        createAndAddRoute("multi", "multi", Identifier.peer("multi"), Identifier.peer("receiver3"));
        createAndAddRoute("multi", "multi", Identifier.peer("multi"), Identifier.peer("receiver4"));

        createAndAddRoute(null, "multi", Identifier.peer("multi"), Identifier.peer("receiver0"));
        createAndAddRoute("multi", null, Identifier.peer("multi"), Identifier.peer("receiver5"));
        createAndAddRoute("multi", "multi", null, Identifier.peer("receiver6"));

        createAndAddRoute(null, "multi", null, Identifier.peer("receiver7"));
        createAndAddRoute(null, null, Identifier.peer("multi"), Identifier.peer("receiver8"));
        createAndAddRoute("multi", null, null, Identifier.peer("receiver9"));

        assertThat(engine.performRouting(createMessage("", "", null)), Matchers.hasSize(0));

        Collection<Identifier> result;

        result = engine.performRouting(createMessage("single", "single", "single"));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("single")));


        result = engine.performRouting(createMessage("type1", "single", "single"));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("single")));

        result = engine.performRouting(createMessage("type2", "single", "single"));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("single")));

        result = engine.performRouting(createMessage("type3", "single", "single"));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("single")));

        result = engine.performRouting(createMessage("type4", "single", "single"));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("single")));


        result = engine.performRouting(createMessage("multi", "subtype1", "single"));
        assertThat(result, Matchers.hasSize(2));
        assertThat(result, Matchers.hasItems(Identifier.peer("single"), Identifier.peer("receiver9")));

        result = engine.performRouting(createMessage("multi", "subtype2", "single"));
        assertThat(result, Matchers.hasSize(2));
        assertThat(result, Matchers.hasItems(Identifier.peer("single"), Identifier.peer("receiver9")));

        result = engine.performRouting(createMessage("multi", "subtype3", "single"));
        assertThat(result, Matchers.hasSize(2));
        assertThat(result, Matchers.hasItems(Identifier.peer("single"), Identifier.peer("receiver9")));

        result = engine.performRouting(createMessage("multi", "subtype4", "single"));
        assertThat(result, Matchers.hasSize(2));
        assertThat(result, Matchers.hasItems(Identifier.peer("single"), Identifier.peer("receiver9")));


        result = engine.performRouting(createMessage("multi", "multi", "peer1"));
        assertThat(result, Matchers.hasSize(4));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("single"), Identifier.peer("receiver9"),
                Identifier.peer("receiver7"), Identifier.peer("receiver6")));

        result = engine.performRouting(createMessage("multi", "multi", "peer2"));
        assertThat(result, Matchers.hasSize(4));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("single"), Identifier.peer("receiver9"),
                Identifier.peer("receiver7"), Identifier.peer("receiver6")));

        result = engine.performRouting(createMessage("multi", "multi", "peer3"));
        assertThat(result, Matchers.hasSize(4));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("single"), Identifier.peer("receiver9"),
                Identifier.peer("receiver7"), Identifier.peer("receiver6")));

        result = engine.performRouting(createMessage("multi", "multi", "peer4"));
        assertThat(result, Matchers.hasSize(4));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("single"), Identifier.peer("receiver9"),
                Identifier.peer("receiver7"), Identifier.peer("receiver6")));


        result = engine.performRouting(createMessage("multi", "multi", "multi"));
        assertThat(result, Matchers.hasSize(10));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("receiver1"), Identifier.peer("receiver2"),
                Identifier.peer("receiver3"), Identifier.peer("receiver4"),
                Identifier.peer("receiver9"), Identifier.peer("receiver7"),
                Identifier.peer("receiver8"), Identifier.peer("receiver6"),
                Identifier.peer("receiver0"), Identifier.peer("receiver5")));


        result = engine.performRouting(createMessage("multi", null, null));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("receiver9")));

        result = engine.performRouting(createMessage(null, "multi", null));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("receiver7")));

        result = engine.performRouting(createMessage(null, null, "multi"));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("receiver8")));
    }

    @Test
     public void testRemoveRouting() throws Exception {
        createAndAddRoute("type1", "subtype", Identifier.peer("peer"), Identifier.peer("receiver"));
        createAndAddRoute("type2", "subtype", Identifier.peer("peer"), Identifier.peer("receiver"));
        Identifier delete = createAndAddRoute("type3", "subtype", Identifier.peer("peer"), Identifier.peer("receiver"));
        createAndAddRoute("type4", "subtype", Identifier.peer("peer"), Identifier.peer("receiver"));

        assertEquals(4, engine.routing.size());
        assertNotNull(engine.routing.get("type1"));
        assertNotNull(engine.routing.get("type2"));
        assertNotNull(engine.routing.get("type3"));
        assertNotNull(engine.routing.get("type4"));

        assertNotNull(engine.removeRouting(delete));
        assertEquals(3, engine.routing.size());
        assertNull(engine.routing.get("type3"));

        createAndAddRoute("type", "subtype1", Identifier.peer("peer"), Identifier.peer("receiver"));
        createAndAddRoute("type", "subtype2", Identifier.peer("peer"), Identifier.peer("receiver"));
        delete = createAndAddRoute("type", "subtype3", Identifier.peer("peer"), Identifier.peer("receiver"));
        createAndAddRoute("type", "subtype4", Identifier.peer("peer"), Identifier.peer("receiver"));

        assertEquals(4, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertEquals(4, engine.routing.get("type").size());
        assertNotNull(engine.routing.get("type").get("subtype1"));
        assertNotNull(engine.routing.get("type").get("subtype2"));
        assertNotNull(engine.routing.get("type").get("subtype3"));
        assertNotNull(engine.routing.get("type").get("subtype4"));

        assertNotNull(engine.removeRouting(delete));
        assertEquals(3, engine.routing.get("type").size());
        assertNull(engine.routing.get("type").get("subtype3"));

        createAndAddRoute("type", "subtype", Identifier.peer("peer1"), Identifier.peer("receiver"));
        createAndAddRoute("type", "subtype", Identifier.peer("peer2"), Identifier.peer("receiver"));
        delete = createAndAddRoute("type", "subtype", Identifier.peer("peer3"), Identifier.peer("receiver"));
        createAndAddRoute("type", "subtype", Identifier.peer("peer4"), Identifier.peer("receiver"));

        assertEquals(4, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertEquals(4, engine.routing.get("type").size());
        assertNotNull(engine.routing.get("type").get("subtype").size());
        assertEquals(4, engine.routing.get("type").get("subtype").size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer1")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer2")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer3")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer4")));

        assertNotNull(engine.removeRouting(delete));
        assertEquals(3, engine.routing.get("type").get("subtype").size());
        assertNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer3")));

        Identifier id1 = createAndAddRoute("type", "subtype", Identifier.peer("peer"), Identifier.peer("receiver1"));
        Identifier id2 = createAndAddRoute("type", "subtype", Identifier.peer("peer"), Identifier.peer("receiver2"));
        Identifier id3 = createAndAddRoute("type", "subtype", Identifier.peer("peer"), Identifier.peer("receiver3"));
        Identifier id4 = createAndAddRoute("type", "subtype", Identifier.peer("peer"), Identifier.peer("receiver3"));

        assertEquals(4, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertEquals(4, engine.routing.get("type").size());
        assertNotNull(engine.routing.get("type").get("subtype"));
        assertEquals(4, engine.routing.get("type").get("subtype").size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")));
        assertEquals(4, engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(id1));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(id2));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(id3));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(id4));

        assertNotNull(engine.removeRouting(id3));
        assertEquals(3, engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).size());
        assertNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(id3));
    }

    private Identifier createAndAddRoute(String type, String subtype, Identifier peer, Identifier receiver) throws InvalidRuleException {
        RoutingRule rule = new RoutingRule(type, subtype, peer, receiver);
        Identifier id = engine.addRouting(rule);

        assertNotNull(engine.routing.get(type));
        assertNotNull(engine.routing.get(type).get(subtype));
        assertNotNull(engine.routing.get(type).get(subtype).get(peer));
        assertEquals(receiver, engine.routing.get(type).get(subtype).get(peer).get(id));

        return id;
    }

    private Message createMessage(String type, String subtype, String peer) {
        return new Message.MessageBuilder().setType(type).setSubtype(subtype).setReceiverId(Identifier.peer(peer)).create();
    }
}