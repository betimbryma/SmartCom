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
package at.ac.tuwien.dsg.smartcom.services;

import at.ac.tuwien.dsg.smartcom.exception.UnknownMessageException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.MessageInformation;
import at.ac.tuwien.dsg.smartcom.services.dao.MongoDBMessageInfoDAO;
import at.ac.tuwien.dsg.smartcom.services.util.FreePortProviderUtil;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.MongoClient;
import org.glassfish.jersey.jackson.JacksonFeature;
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

import static org.junit.Assert.*;

public class MessageInfoServiceImplTest {

    public String url;
    private MongoDBInstance mongoDB;

    private MongoDBMessageInfoDAO dao;

    private Client client;
    private MessageInfoServiceImpl service;

    @Before
    public void setUp() throws Exception {
        int mongoDbPort = FreePortProviderUtil.getFreePort();
        mongoDB = new MongoDBInstance(mongoDbPort);
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", mongoDbPort);
        dao = new MongoDBMessageInfoDAO(mongo, "TEST", "MIS");

        this.client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
//                .property(ClientProperties.CONNECT_TIMEOUT, 5000)
//                .property(ClientProperties.READ_TIMEOUT, 5000)
                .build();
//        client.register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true)); //enables this to have additional logging information

        int freePort = FreePortProviderUtil.getFreePort();
        url = "http://localhost:"+freePort+"/mis";
        service = new MessageInfoServiceImpl(freePort, "mis", dao);
        service.init();
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
        client.close();
        service.cleanUp();
    }

    @Test
    public void testGetInfoForMessageREST() throws Exception {
        MessageInformation info1 = createMessageInformation("TASK1", "REQUEST1", "simple request", "any response is valid");
        MessageInformation info2 = createMessageInformation("TASK2", "REQUEST2", "simple request", "any response is valid");
        MessageInformation info3 = createMessageInformation("TASK3", "REQUEST3", "simple request", "any response is valid");

        dao.insert(info1);
        dao.insert(info2);
        dao.insert(info3);

        assertEquals(info1, getInfoByREST(info1));
        assertEquals(info2, getInfoByREST(info2));
        assertEquals(info3, getInfoByREST(info3));

        MessageInformation info4 = createMessageInformation("TASK", "REQUEST", "simple request", "any response is valid");
        dao.insert(info4);

        assertEquals(info4, getInfoByREST(info4));
        assertEquals("simple request", info4.getPurpose());

        info4.setPurpose("special request");
        service.addMessageInfo(createMessageFromMessageInfo(info4), info4);

        assertEquals(info4, getInfoByREST(info4));
        assertEquals("special request", info4.getPurpose());

        MessageInformation info = new MessageInformation();
        info.setKey(new MessageInformation.Key("TASK4", "REQUEST4"));

        assertNull(getInfoByREST(info));
    }

    @Test
    public void testAddMessageInfo() throws Exception {
        MessageInformation info1 = createMessageInformation("TASK1", "REQUEST1", "simple request", "any response is valid");
        MessageInformation info2 = createMessageInformation("TASK2", "REQUEST2", "simple request", "any response is valid");
        MessageInformation info3 = createMessageInformation("TASK3", "REQUEST3", "simple request", "any response is valid");

        service.addMessageInfo(createMessageFromMessageInfo(info1), info1);
        service.addMessageInfo(createMessageFromMessageInfo(info2), info2);
        service.addMessageInfo(createMessageFromMessageInfo(info3), info3);

        assertEquals(info1, dao.find(info1.getKey()));
        assertEquals(info2, dao.find(info2.getKey()));
        assertEquals(info3, dao.find(info3.getKey()));

        assertEquals(info1, service.getInfoForMessage(createMessageFromMessageInfo(info1)));
        assertEquals(info2, service.getInfoForMessage(createMessageFromMessageInfo(info2)));
        assertEquals(info3, service.getInfoForMessage(createMessageFromMessageInfo(info3)));

        MessageInformation info4 = createMessageInformation("TASK", "REQUEST", "simple request", "any response is valid");
        service.addMessageInfo(createMessageFromMessageInfo(info4), info4);

        assertEquals(info4, dao.find(info4.getKey()));
        assertEquals(info4, service.getInfoForMessage(createMessageFromMessageInfo(info4)));
        assertEquals("simple request", info4.getPurpose());

        info4.setPurpose("special request");
        service.addMessageInfo(createMessageFromMessageInfo(info4), info4);

        assertEquals(info4, dao.find(info4.getKey()));
        assertEquals(info4, service.getInfoForMessage(createMessageFromMessageInfo(info4)));
        assertEquals("special request", info4.getPurpose());
    }

    @Test
    public void testGetInfoForMessage() throws Exception {
        MessageInformation info1 = createMessageInformation("TASK1", "REQUEST1", "simple request", "any response is valid");
        MessageInformation info2 = createMessageInformation("TASK2", "REQUEST2", "simple request", "any response is valid");
        MessageInformation info3 = createMessageInformation("TASK3", "REQUEST3", "simple request", "any response is valid");

        dao.insert(info1);
        dao.insert(info2);
        dao.insert(info3);

        assertEquals(info1, service.getInfoForMessage(createMessageFromMessageInfo(info1)));
        assertEquals(info2, service.getInfoForMessage(createMessageFromMessageInfo(info2)));
        assertEquals(info3, service.getInfoForMessage(createMessageFromMessageInfo(info3)));

        MessageInformation info4 = createMessageInformation("TASK", "REQUEST", "simple request", "any response is valid");
        dao.insert(info4);

        assertEquals(info4, service.getInfoForMessage(createMessageFromMessageInfo(info4)));
        assertEquals("simple request", info4.getPurpose());

        info4.setPurpose("special request");
        service.addMessageInfo(createMessageFromMessageInfo(info4), info4);

        assertEquals(info4, service.getInfoForMessage(createMessageFromMessageInfo(info4)));
        assertEquals("special request", info4.getPurpose());

        MessageInformation info = new MessageInformation();
        info.setKey(new MessageInformation.Key("TASK4", "REQUEST4"));

        try {
            service.getInfoForMessage(createMessageFromMessageInfo(info));
            fail("no exception thrown!");
        } catch (UnknownMessageException ignored) {

        }
    }

    @Test
    public void testAddMessageInfoREST() throws Exception {
        MessageInformation info1 = createMessageInformation("TASK1", "REQUEST1", "simple request", "any response is valid");
        MessageInformation info2 = createMessageInformation("TASK2", "REQUEST2", "simple request", "any response is valid");
        MessageInformation info3 = createMessageInformation("TASK3", "REQUEST3", "simple request", "any response is valid");

        postInfoByREST(info1);
        postInfoByREST(info2);
        postInfoByREST(info3);

        assertEquals(info1, dao.find(info1.getKey()));
        assertEquals(info2, dao.find(info2.getKey()));
        assertEquals(info3, dao.find(info3.getKey()));

        assertEquals(info1, service.getInfoForMessage(createMessageFromMessageInfo(info1)));
        assertEquals(info2, service.getInfoForMessage(createMessageFromMessageInfo(info2)));
        assertEquals(info3, service.getInfoForMessage(createMessageFromMessageInfo(info3)));

        MessageInformation info4 = createMessageInformation("TASK", "REQUEST", "simple request", "any response is valid");
        postInfoByREST(info4);

        assertEquals(info4, dao.find(info4.getKey()));
        assertEquals(info4, service.getInfoForMessage(createMessageFromMessageInfo(info4)));
        assertEquals("simple request", info4.getPurpose());

        info4.setPurpose("special request");
        postInfoByREST(info4);

        assertEquals(info4, dao.find(info4.getKey()));
        assertEquals(info4, service.getInfoForMessage(createMessageFromMessageInfo(info4)));
        assertEquals("special request", info4.getPurpose());
    }

    private void postInfoByREST(MessageInformation info) {
        WebTarget target = client.target(url);

        target.request(MediaType.APPLICATION_JSON).post(Entity.json(info));
    }


    private MessageInformation getInfoByREST(MessageInformation info) {
        WebTarget target = client.target(url);
        target = target.queryParam("type", info.getKey().getType());
        target = target.queryParam("subtype", info.getKey().getSubtype());

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
            return null;
        }

        return response.readEntity(MessageInformation.class);
    }

    private static MessageInformation createMessageInformation(String type, String subtype, String purpose, String validAnswer) {
        MessageInformation info = new MessageInformation();
        info.setKey(new MessageInformation.Key(type, subtype));
        info.setPurpose(purpose);
        info.setValidAnswer(validAnswer);
        info.setRelatedMessages(Arrays.asList(
                new MessageInformation.Key("TASK", "INFO"),
                new MessageInformation.Key("TASK", "ERROR")));
        info.setValidAnswerTypes(Arrays.asList(
                new MessageInformation.Key("TASK", "RESPONSE"),
                new MessageInformation.Key("TASK", "DECLINED")));

        return info;
    }

    private static Message createMessageFromMessageInfo(MessageInformation information) {
        return new Message.MessageBuilder()
                .setType(information.getKey().getType())
                .setSubtype(information.getKey().getSubtype())
                .create();
    }
}