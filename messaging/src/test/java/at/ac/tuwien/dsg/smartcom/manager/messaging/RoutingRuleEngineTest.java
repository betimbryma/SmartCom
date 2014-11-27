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
        RoutingRule rule = new RoutingRule("type", "subtype", Identifier.peer("peer"), Identifier.peer("sender"), null);
        engine.addRouting(rule);
    }

    @Test(expected = InvalidRuleException.class)
    public void testAddRouting_error2() throws Exception {
        RoutingRule rule = new RoutingRule("", "", Identifier.peer(null), Identifier.peer(""), Identifier.peer("receiver"));
        engine.addRouting(rule);
    }

    @Test
    public void testAddRouting_nullValues() throws Exception {
        testAndCheckAdding(1, "type",   "subtype",  "peer", "sender", "receiver");

        testAndCheckAdding(2,  null,    "subtype",  "peer", "sender", "receiver");
        testAndCheckAdding(2, "type",    null,      "peer", "sender", "receiver");
        testAndCheckAdding(2, "type",   "subtype",   null,  "sender", "receiver");
        testAndCheckAdding(2, "type",   "subtype",  "peer",  null,    "receiver");

        testAndCheckAdding(2, null,      null,      "peer", "sender", "receiver");
        testAndCheckAdding(2, null,     "subtype",   null,  "sender", "receiver");
        testAndCheckAdding(2, null,     "subtype",  "peer",  null,    "receiver");

        testAndCheckAdding(2, "type",    null,       null,  "sender", "receiver");
        testAndCheckAdding(2, "type",    null,      "peer",  null,    "receiver");

        testAndCheckAdding(2, "type",   "subtype",   null,   null,    "receiver");

        testAndCheckAdding(2, "type",   null,        null,   null,    "receiver");
        testAndCheckAdding(2,  null,   "subtype",    null,   null,    "receiver");
        testAndCheckAdding(2,  null,    null,       "peer",  null,    "receiver");
        testAndCheckAdding(2,  null,    null,        null,  "sender", "receiver");
    }

    private void testAndCheckAdding(int size, String type, String subtype, String receiverId, String senderId, String routeId) throws InvalidRuleException {
        Identifier receiver = (receiverId == null ? null : Identifier.peer(receiverId));
        Identifier sender = (senderId == null ? null : Identifier.peer(senderId));
        Identifier route = (routeId == null ? null : Identifier.peer(routeId));
        RoutingRule rule = new RoutingRule(type, subtype, receiver, sender, route);

        Identifier identifier = engine.addRouting(rule);
        assertEquals(size, engine.routing.size());
        assertNotNull(engine.routing.get(type));
        assertNotNull(engine.routing.get(type).get(subtype));
        assertNotNull(engine.routing.get(type).get(subtype).get(receiver));
        assertNotNull(engine.routing.get(type).get(subtype).get(receiver).get(sender));
        assertNotNull(engine.routing.get(type).get(subtype).get(receiver).get(sender).get(identifier));
        assertEquals(route, engine.routing.get(type).get(subtype).get(receiver).get(sender).get(identifier));
    }

    @Test
    public void testAddRouting_multiType() throws Exception {
        createAndAddRoute("type1", "subtype", "peer", "sender", "receiver");
        createAndAddRoute("type2", "subtype", "peer", "sender", "receiver");
        createAndAddRoute("type3", "subtype", "peer", "sender", "receiver");
        createAndAddRoute("type4", "subtype", "peer", "sender", "receiver");

        assertEquals(4, engine.routing.size());
        assertNotNull(engine.routing.get("type1"));
        assertNotNull(engine.routing.get("type2"));
        assertNotNull(engine.routing.get("type3"));
        assertNotNull(engine.routing.get("type4"));

        createAndAddRoute("type", "subtype1", "peer", "sender", "receiver");
        createAndAddRoute("type", "subtype2", "peer", "sender", "receiver");
        createAndAddRoute("type", "subtype3", "peer", "sender", "receiver");
        createAndAddRoute("type", "subtype4", "peer", "sender", "receiver");

        assertEquals(5, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertEquals(4, engine.routing.get("type").size());
        assertNotNull(engine.routing.get("type").get("subtype1"));
        assertNotNull(engine.routing.get("type").get("subtype2"));
        assertNotNull(engine.routing.get("type").get("subtype3"));
        assertNotNull(engine.routing.get("type").get("subtype4"));

        createAndAddRoute("type", "subtype", "peer1", "sender", "receiver");
        createAndAddRoute("type", "subtype", "peer2", "sender", "receiver");
        createAndAddRoute("type", "subtype", "peer3", "sender", "receiver");
        createAndAddRoute("type", "subtype", "peer4", "sender", "receiver");

        assertEquals(5, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertEquals(5, engine.routing.get("type").size());
        assertNotNull(engine.routing.get("type").get("subtype").size());
        assertEquals(4, engine.routing.get("type").get("subtype").size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer1")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer2")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer3")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer4")));

        createAndAddRoute("type", "subtype", "peer", "sender1", "receiver");
        createAndAddRoute("type", "subtype", "peer", "sender2", "receiver");
        createAndAddRoute("type", "subtype", "peer", "sender3", "receiver");
        createAndAddRoute("type", "subtype", "peer", "sender4", "receiver");

        assertEquals(5, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertEquals(5, engine.routing.get("type").size());
        assertNotNull(engine.routing.get("type").get("subtype").size());
        assertEquals(5, engine.routing.get("type").get("subtype").size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")));
        assertEquals(4, engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender1")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender2")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender3")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender4")));

        Identifier id1 = createAndAddRoute("type", "subtype", "peer", "sender", "receiver1");
        Identifier id2 = createAndAddRoute("type", "subtype", "peer", "sender", "receiver2");
        Identifier id3 = createAndAddRoute("type", "subtype", "peer", "sender", "receiver3");
        Identifier id4 = createAndAddRoute("type", "subtype", "peer", "sender", "receiver3");

        assertEquals(5, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertEquals(5, engine.routing.get("type").size());
        assertNotNull(engine.routing.get("type").get("subtype").size());
        assertEquals(5, engine.routing.get("type").get("subtype").size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")));
        assertEquals(5, engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender")));
        assertEquals(4, engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender")).size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender")).get(id1));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender")).get(id2));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender")).get(id3));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender")).get(id4));
    }

    @Test
    public void testPerformRouting_empty() throws Exception {
        assertThat(engine.performRouting(createMessage("type", "subtype", "peer", "receiver")), Matchers.hasSize(0));
    }

    @Test
    public void testPerformRouting() throws Exception {
        createAndAddRoute("single", "single", "single", "single", "single");

        createAndAddRoute("type1", "single", "single", "single", "single");
        createAndAddRoute("type2", "single", "single", "single", "single");
        createAndAddRoute("type3", "single", "single", "single", "single");
        createAndAddRoute("type4", "single", "single", "single", "single");

        createAndAddRoute("multi", "subtype1", "single", "single", "single");
        createAndAddRoute("multi", "subtype2", "single", "single", "single");
        createAndAddRoute("multi", "subtype3", "single", "single", "single");
        createAndAddRoute("multi", "subtype4", "single", "single", "single");

        createAndAddRoute("multi", "multi", "peer1", "single", "single");
        createAndAddRoute("multi", "multi", "peer2", "single", "single");
        createAndAddRoute("multi", "multi", "peer3", "single", "single");
        createAndAddRoute("multi", "multi", "peer4", "single", "single");

        createAndAddRoute("multi", "multi", "multi", "sender1", "single");
        createAndAddRoute("multi", "multi", "multi", "sender2", "single");
        createAndAddRoute("multi", "multi", "multi", "sender3", "single");
        createAndAddRoute("multi", "multi", "multi", "sender4", "single");

        createAndAddRoute("multi", "multi", "multi", "multi", "receiver1");
        createAndAddRoute("multi", "multi", "multi", "multi", "receiver2");
        createAndAddRoute("multi", "multi", "multi", "multi", "receiver3");
        createAndAddRoute("multi", "multi", "multi", "multi", "receiver4");

        createAndAddRoute(null, "multi", "multi", "multi", "receiver0");
        createAndAddRoute("multi", null, "multi", "multi", "receiver5");
        createAndAddRoute("multi", "multi", null, "multi", "receiver6");
        createAndAddRoute("multi", "multi", "multi", null, "receiver10");

        createAndAddRoute(null, null, "multi", "multi", "receiver8");
        createAndAddRoute(null, "multi", null, "multi", "receiver7");
        createAndAddRoute(null, "multi", "multi", null, "receiver11");
        createAndAddRoute("multi", null, null, "multi", "receiver9");
        createAndAddRoute("multi", null, "multi", null, "receiver12");
        createAndAddRoute("multi", "multi", null, null, "receiver13");

        createAndAddRoute("multi", null, null, null, "receiver14");
        createAndAddRoute(null, "multi", null, null, "receiver15");
        createAndAddRoute(null, null, "multi", null, "receiver16");
        createAndAddRoute(null, null, null, "multi", "receiver17");

        assertThat(engine.performRouting(createMessage("", "", null, null)), Matchers.hasSize(0));

        Collection<Identifier> result;

        result = engine.performRouting(createMessage("single", "single", "single", "single"));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("single")));


        result = engine.performRouting(createMessage("type1", "single", "single", "single"));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("single")));

        result = engine.performRouting(createMessage("type2", "single", "single", "single"));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("single")));

        result = engine.performRouting(createMessage("type3", "single", "single", "single"));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("single")));

        result = engine.performRouting(createMessage("type4", "single", "single", "single"));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("single")));


        result = engine.performRouting(createMessage("multi", "subtype1", "single", "single"));
        assertThat(result, Matchers.hasSize(2));
        assertThat(result, Matchers.hasItems(Identifier.peer("single"), Identifier.peer("receiver14")));

        result = engine.performRouting(createMessage("multi", "subtype2", "single", "single"));
        assertThat(result, Matchers.hasSize(2));
        assertThat(result, Matchers.hasItems(Identifier.peer("single"), Identifier.peer("receiver14")));

        result = engine.performRouting(createMessage("multi", "subtype3", "single", "single"));
        assertThat(result, Matchers.hasSize(2));
        assertThat(result, Matchers.hasItems(Identifier.peer("single"), Identifier.peer("receiver14")));

        result = engine.performRouting(createMessage("multi", "subtype4", "single", "single"));
        assertThat(result, Matchers.hasSize(2));
        assertThat(result, Matchers.hasItems(Identifier.peer("single"), Identifier.peer("receiver14")));


        result = engine.performRouting(createMessage("multi", "multi", "peer1", "single"));
        assertThat(result, Matchers.hasSize(4));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("single"), Identifier.peer("receiver14"),
                Identifier.peer("receiver15"), Identifier.peer("receiver13")));

        result = engine.performRouting(createMessage("multi", "multi", "peer2", "single"));
        assertThat(result, Matchers.hasSize(4));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("single"), Identifier.peer("receiver14"),
                Identifier.peer("receiver15"), Identifier.peer("receiver13")));

        result = engine.performRouting(createMessage("multi", "multi", "peer3", "single"));
        assertThat(result, Matchers.hasSize(4));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("single"), Identifier.peer("receiver14"),
                Identifier.peer("receiver15"), Identifier.peer("receiver13")));

        result = engine.performRouting(createMessage("multi", "multi", "peer4", "single"));
        assertThat(result, Matchers.hasSize(4));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("single"), Identifier.peer("receiver14"),
                Identifier.peer("receiver15"), Identifier.peer("receiver13")));


        result = engine.performRouting(createMessage("multi", "multi", "multi", "sender1"));
        assertThat(result, Matchers.hasSize(8));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("single"), Identifier.peer("receiver14"),
                Identifier.peer("receiver15"), Identifier.peer("receiver13"),
                Identifier.peer("receiver10"), Identifier.peer("receiver11"),
                Identifier.peer("receiver12"), Identifier.peer("receiver16")));

        result = engine.performRouting(createMessage("multi", "multi", "multi", "sender2"));
        assertThat(result, Matchers.hasSize(8));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("single"), Identifier.peer("receiver14"),
                Identifier.peer("receiver15"), Identifier.peer("receiver13"),
                Identifier.peer("receiver10"), Identifier.peer("receiver11"),
                Identifier.peer("receiver12"), Identifier.peer("receiver16")));

        result = engine.performRouting(createMessage("multi", "multi", "multi", "sender3"));
        assertThat(result, Matchers.hasSize(8));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("single"), Identifier.peer("receiver14"),
                Identifier.peer("receiver15"), Identifier.peer("receiver13"),
                Identifier.peer("receiver10"), Identifier.peer("receiver11"),
                Identifier.peer("receiver12"), Identifier.peer("receiver16")));

        result = engine.performRouting(createMessage("multi", "multi", "multi", "sender4"));
        assertThat(result, Matchers.hasSize(8));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("single"), Identifier.peer("receiver14"),
                Identifier.peer("receiver15"), Identifier.peer("receiver13"),
                Identifier.peer("receiver10"), Identifier.peer("receiver11"),
                Identifier.peer("receiver12"), Identifier.peer("receiver16")));


        result = engine.performRouting(createMessage("multi", "multi", "multi", "multi"));
        assertThat(result, Matchers.hasSize(18));
        assertThat(result, Matchers.hasItems(
                Identifier.peer("receiver0"), Identifier.peer("receiver1"),
                Identifier.peer("receiver2"), Identifier.peer("receiver3"),
                Identifier.peer("receiver4"), Identifier.peer("receiver5"),
                Identifier.peer("receiver6"), Identifier.peer("receiver7"),
                Identifier.peer("receiver8"), Identifier.peer("receiver9"),
                Identifier.peer("receiver10"),Identifier.peer("receiver11"),
                Identifier.peer("receiver12"),Identifier.peer("receiver13"),
                Identifier.peer("receiver14"),Identifier.peer("receiver15"),
                Identifier.peer("receiver16"),Identifier.peer("receiver17")));


        result = engine.performRouting(createMessage("multi", null, null, null));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("receiver14")));

        result = engine.performRouting(createMessage(null, "multi", null, null));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("receiver15")));

        result = engine.performRouting(createMessage(null, null, "multi", null));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("receiver16")));

        result = engine.performRouting(createMessage(null, null, null, "multi"));
        assertThat(result, Matchers.hasSize(1));
        assertThat(result, Matchers.hasItem(Identifier.peer("receiver17")));
    }

    @Test
     public void testRemoveRouting() throws Exception {
        createAndAddRoute("type1", "subtype", "peer", "sender", "receiver");
        createAndAddRoute("type2", "subtype", "peer", "sender", "receiver");
        Identifier delete = createAndAddRoute("type3", "subtype", "peer", "sender", "receiver");
        createAndAddRoute("type4", "subtype", "peer", "sender", "receiver");

        assertEquals(4, engine.routing.size());
        assertNotNull(engine.routing.get("type1"));
        assertNotNull(engine.routing.get("type2"));
        assertNotNull(engine.routing.get("type3"));
        assertNotNull(engine.routing.get("type4"));

        assertNotNull(engine.removeRouting(delete));
        assertEquals(3, engine.routing.size());
        assertNull(engine.routing.get("type3"));


        createAndAddRoute("type", "subtype1", "peer", "sender", "receiver");
        createAndAddRoute("type", "subtype2", "peer", "sender", "receiver");
        delete = createAndAddRoute("type", "subtype3", "peer", "sender", "receiver");
        createAndAddRoute("type", "subtype4", "peer", "sender", "receiver");

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

        createAndAddRoute("type", "subtype", "peer1", "sender", "receiver");
        createAndAddRoute("type", "subtype", "peer2", "sender", "receiver");
        delete = createAndAddRoute("type", "subtype", "peer3", "sender", "receiver");
        createAndAddRoute("type", "subtype", "peer4", "sender", "receiver");

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


        createAndAddRoute("type", "subtype", "peer", "sender1", "receiver");
        createAndAddRoute("type", "subtype", "peer", "sender2", "receiver");
        delete = createAndAddRoute("type", "subtype", "peer", "sender3", "receiver");
        createAndAddRoute("type", "subtype", "peer", "sender4", "receiver");

        assertEquals(4, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertEquals(4, engine.routing.get("type").size());
        assertNotNull(engine.routing.get("type").get("subtype").size());
        assertEquals(4, engine.routing.get("type").get("subtype").size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")));
        assertEquals(4, engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender1")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender2")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender3")));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender4")));

        assertNotNull(engine.removeRouting(delete));
        assertEquals(3, engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).size());
        assertNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer3")));


        Identifier id1 = createAndAddRoute("type", "subtype", "peer", "sender", "receiver1");
        Identifier id2 = createAndAddRoute("type", "subtype", "peer", "sender", "receiver2");
        Identifier id3 = createAndAddRoute("type", "subtype", "peer", "sender", "receiver3");
        Identifier id4 = createAndAddRoute("type", "subtype", "peer", "sender", "receiver3");

        assertEquals(4, engine.routing.size());
        assertNotNull(engine.routing.get("type"));
        assertEquals(4, engine.routing.get("type").size());
        assertNotNull(engine.routing.get("type").get("subtype"));
        assertEquals(4, engine.routing.get("type").get("subtype").size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")));
        assertEquals(4, engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).size());
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender")).get(id1));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender")).get(id2));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender")).get(id3));
        assertNotNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender")).get(id4));

        assertNotNull(engine.removeRouting(id3));
        assertEquals(3, engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(Identifier.peer("sender")).size());
        assertNull(engine.routing.get("type").get("subtype").get(Identifier.peer("peer")).get(id3));
    }

    private Identifier createAndAddRoute(String type, String subtype, String receiverId, String senderId, String routeId) throws InvalidRuleException {
        Identifier receiver = (receiverId == null ? null : Identifier.peer(receiverId));
        Identifier sender = (senderId == null ? null : Identifier.peer(senderId));
        Identifier route = (routeId == null ? null : Identifier.peer(routeId));
        RoutingRule rule = new RoutingRule(type, subtype, receiver, sender, route);
        Identifier id = engine.addRouting(rule);

        assertNotNull(engine.routing.get(type));
        assertNotNull(engine.routing.get(type).get(subtype));
        assertNotNull(engine.routing.get(type).get(subtype).get(receiver));
        assertEquals(route, engine.routing.get(type).get(subtype).get(receiver).get(sender).get(id));

        return id;
    }

    private Message createMessage(String type, String subtype, String receiver, String sender) {
        return new Message.MessageBuilder()
                .setType(type)
                .setSubtype(subtype)
                .setReceiverId(Identifier.peer(receiver))
                .setSenderId(Identifier.peer(sender))
                .create();
    }
}