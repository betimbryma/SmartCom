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
package at.ac.tuwien.dsg.peer.dao;

import at.ac.tuwien.dsg.smartcom.adapters.rest.JsonMessageDTO;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.MongoClient;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MongoDBPeerMailboxDAOTest {

    private MongoDBInstance mongoDB;

    private MongoDBPeerMailboxDAO dao;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", 12345);
        dao = new MongoDBPeerMailboxDAO(mongo, "TEST", "PEER");
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
    }

    @Test
    public void testPersistMessageAndGetMessagesForReceiver() throws Exception {
        String receiver = "testReceiver";
        List<JsonMessageDTO> messages = dao.getMessagesForReceiver(receiver);
        assertThat(messages, Matchers.hasSize(0));

        dao.persistMessage(createMessageWrapper("1"), "sender1");
        dao.persistMessage(createMessageWrapper("2"), "sender1");
        dao.persistMessage(createMessageWrapper("3"), "sender1");
        dao.persistMessage(createMessageWrapper("1"), "sender2");
        dao.persistMessage(createMessageWrapper("1"), "sender3");

        messages = dao.getMessagesForReceiver("sender1");
        assertThat(messages, Matchers.hasSize(3));

        messages = dao.getMessagesForReceiver("sender2");
        assertThat(messages, Matchers.hasSize(1));

        messages = dao.getMessagesForReceiver("sender3");
        assertThat(messages, Matchers.hasSize(1));
    }

    @Test
    public void testPullMessageForReceiver() throws Exception {
        dao.persistMessage(createMessageWrapper("1"), "sender1");
        dao.persistMessage(createMessageWrapper("2"), "sender1");
        dao.persistMessage(createMessageWrapper("3"), "sender1");
        dao.persistMessage(createMessageWrapper("1"), "sender2");
        dao.persistMessage(createMessageWrapper("1"), "sender3");

        JsonMessageDTO dto = dao.pullMessageForReceiver("sender2");
        assertNotNull(dto);
        assertEquals("1", dto.getId());

        dto = dao.pullMessageForReceiver("sender1");
        assertNotNull(dto);
        assertEquals("1", dto.getId());

        dto = dao.pullMessageForReceiver("sender3");
        assertNotNull(dto);
        assertEquals("1", dto.getId());

        dto = dao.pullMessageForReceiver("sender1");
        assertNotNull(dto);
        assertEquals("2", dto.getId());

        dto = dao.pullMessageForReceiver("sender1");
        assertNotNull(dto);
        assertEquals("3", dto.getId());

        dto = dao.pullMessageForReceiver("sender1");
        assertNull(dto);
    }

    private JsonMessageDTO createMessageWrapper(String id) {
        return createMessage("content"+id, "type"+id, "subtype"+id, "sender"+id, id, "language"+id, "securityToken"+id, "conversation"+id);
    }

    private JsonMessageDTO createMessage(String content, String type, String subtype,
                                         String sender, String id, String language,
                                         String securityToken, String conversation) {
        JsonMessageDTO dto = new JsonMessageDTO();
        dto.setContent(content);
        dto.setType(type);
        dto.setSubtype(subtype);
        dto.setSender(sender);
        dto.setId(id);
        dto.setConversation(conversation);
        dto.setLanguage(language);
        dto.setSecurityToken(securityToken);

        return dto;
    }
}