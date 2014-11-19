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
package at.ac.tuwien.dsg.peer;

import at.ac.tuwien.dsg.peer.dao.MongoDBPeerMailboxDAO;
import at.ac.tuwien.dsg.peer.util.FreePortProviderUtil;
import at.ac.tuwien.dsg.smartcom.adapters.rest.JsonMessageDTO;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.MongoClient;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PeerMailboxServiceTest {

    private MongoDBInstance mongoDB;
    private MongoDBPeerMailboxDAO mailboxDao;
    private Client client;
    private String url;
    private PeerMailboxService mailbox;

    @Before
    public void setUp() throws Exception {
        int mongoDbPort = FreePortProviderUtil.getFreePort();
        mongoDB = new MongoDBInstance(mongoDbPort);
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", mongoDbPort);
        mailboxDao = new MongoDBPeerMailboxDAO(mongo, "TEST", "PEER");

        this.client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
                .register(MultiPartFeature.class)
//                .property(ClientProperties.CONNECT_TIMEOUT, 5000)
//                .property(ClientProperties.READ_TIMEOUT, 5000)
                .build();
        client.register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true)); //enables this to have additional logging information

        int freePort = FreePortProviderUtil.getFreePort();
        url = "http://localhost:"+freePort+"/mailbox";
        mailbox = new PeerMailboxService(freePort, "mailbox", mailboxDao);
        mailbox.init();
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
        client.close();
        mailbox.cleanUp();
    }

    @Test
    public void testMailboxPollingAll() throws Exception {
        String receiver = "testReceiver";
        List<JsonMessageDTO> messages = pollall(receiver);
        assertThat(messages, Matchers.hasSize(0));

        sendNewMessage(createMessageWrapper("1"), "sender1");
        sendNewMessage(createMessageWrapper("2"), "sender1");
        sendNewMessage(createMessageWrapper("3"), "sender1");
        sendNewMessage(createMessageWrapper("1"), "sender2");
        sendNewMessage(createMessageWrapper("1"), "sender3");

        messages = pollall("sender1");
        assertThat(messages, Matchers.hasSize(3));

        messages = pollall("sender2");
        assertThat(messages, Matchers.hasSize(1));

        messages = pollall("sender3");
        assertThat(messages, Matchers.hasSize(1));
    }

    @Test
    public void testMailboxPolling() throws Exception {
        sendNewMessage(createMessageWrapper("1"), "sender1");
        sendNewMessage(createMessageWrapper("2"), "sender1");
        sendNewMessage(createMessageWrapper("3"), "sender1");
        sendNewMessage(createMessageWrapper("1"), "sender2");
        sendNewMessage(createMessageWrapper("1"), "sender3");

        JsonMessageDTO dto = poll("sender2");
        assertNotNull(dto);
        assertEquals("1", dto.getId());

        dto = poll("sender1");
        assertNotNull(dto);
        assertEquals("1", dto.getId());

        dto = poll("sender3");
        assertNotNull(dto);
        assertEquals("1", dto.getId());

        dto = poll("sender1");
        assertNotNull(dto);
        assertEquals("2", dto.getId());

        dto = poll("sender1");
        assertNotNull(dto);
        assertEquals("3", dto.getId());

        dto = poll("sender1");
        assertNull(dto);
    }

    @Test
    public void testMailboxLongPolling() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                sendNewMessage(createMessageWrapper("1"), "sender1");
            }
        }).start();

        JsonMessageDTO dto = longpoll("sender1");
        assertNotNull(dto);
        assertEquals("1", dto.getId());

        sendNewMessage(createMessageWrapper("2"), "sender1");
        sendNewMessage(createMessageWrapper("3"), "sender1");
        dto = longpoll("sender1");
        assertNotNull(dto);
        assertEquals("2", dto.getId());
    }

    public void sendNewMessage(JsonMessageDTO message, String receiver) {
        WebTarget target = client.target(url + "/"+receiver);
        Response post = target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(message));
        assertEquals(Response.Status.OK.getStatusCode(), post.getStatus());
    }

    public JsonMessageDTO poll(String receiver) {
        WebTarget target = client.target(url + "/"+receiver);
        Response get = target.request(MediaType.APPLICATION_JSON_TYPE).get();
        assertThat(get.getStatus(),
                Matchers.either(Matchers.equalTo(Response.Status.OK.getStatusCode()))
                        .or(Matchers.equalTo(Response.Status.NO_CONTENT.getStatusCode())));

        return get.readEntity(JsonMessageDTO.class);
    }

    public List<JsonMessageDTO> pollall(String receiver) {
        WebTarget target = client.target(url + "/all/"+receiver);
        Response get = target.request(MediaType.APPLICATION_JSON_TYPE).get();
        assertThat(get.getStatus(),
                Matchers.either(Matchers.equalTo(Response.Status.OK.getStatusCode()))
                        .or(Matchers.equalTo(Response.Status.NO_CONTENT.getStatusCode())));

        return Arrays.asList(get.readEntity(JsonMessageDTO[].class));
    }

    public JsonMessageDTO longpoll(String receiver) {
        WebTarget target = client.target(url + "/poll/"+receiver);
        Response get = target.request(MediaType.APPLICATION_JSON_TYPE).get();
        assertThat(get.getStatus(),
                Matchers.either(Matchers.equalTo(Response.Status.OK.getStatusCode()))
                        .or(Matchers.equalTo(Response.Status.NO_CONTENT.getStatusCode())));

        return get.readEntity(JsonMessageDTO.class);
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