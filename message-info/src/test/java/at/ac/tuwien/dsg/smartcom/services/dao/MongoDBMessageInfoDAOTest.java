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
package at.ac.tuwien.dsg.smartcom.services.dao;

import at.ac.tuwien.dsg.smartcom.model.MessageInformation;
import at.ac.tuwien.dsg.smartcom.services.util.FreePortProviderUtil;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class MongoDBMessageInfoDAOTest {

    private MongoDBInstance mongoDB;

    private MongoDBMessageInfoDAO dao;
    private DBCollection collection;

    @Before
    public void setUp() throws Exception {
        int mongoDbPort = FreePortProviderUtil.getFreePort();
        mongoDB = new MongoDBInstance(mongoDbPort);
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", mongoDbPort);
        collection = mongo.getDB("test-mi").getCollection("mi");
        dao = new MongoDBMessageInfoDAO(mongo, "test-mi", "mi");
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
    }

    @Test
    public void testInsert() throws Exception {
        MessageInformation info1 = createMessageInformation("TASK1", "REQUEST1", "simple request", "any response is valid");
        MessageInformation info2 = createMessageInformation("TASK2", "REQUEST2", "simple request", "any response is valid");
        MessageInformation info3 = createMessageInformation("TASK3", "REQUEST3", "simple request", "any response is valid");

        dao.insert(info1);
        dao.insert(info2);
        dao.insert(info3);

        assertEquals("Not enough message information entries saved!", 3, collection.count());

        List<MessageInformation> infos = new ArrayList<>(3);
        for (DBObject dbObject : collection.find()) {
            MessageInformation info = dao.deserializeMessageInfo(dbObject);
            assertNotNull(info);
            assertNotNull(info.getKey());
            assertNotNull(info.getPurpose());
            assertNotNull(info.getValidAnswer());
            assertNotNull(info.getValidAnswerTypes());
            assertNotNull(info.getRelatedMessages());

            infos.add(info);
        }

        assertThat(infos, Matchers.hasSize(3));
        assertThat(infos, Matchers.contains(info1, info2, info3));

        MessageInformation info4 = createMessageInformation("TASK", "REQUEST", "simple request", "any response is valid");
        dao.insert(info4);

        assertEquals("Not enough message information entries saved!", 4, collection.count());

        MessageInformation info = dao.deserializeMessageInfo(collection.findOne(new BasicDBObject("_id", dao.serializeKey(info4.getKey()))));
        assertNotNull(info);
        assertEquals(info4, info);
        assertEquals(info.getPurpose(), "simple request");

        info4.setPurpose("special request");
        dao.insert(info4);

        info = dao.deserializeMessageInfo(collection.findOne(new BasicDBObject("_id", dao.serializeKey(info4.getKey()))));
        assertNotNull(info);
        assertEquals(info4, info);
        assertEquals(info.getPurpose(), "special request");
    }

    @Test
    public void testFind() throws Exception {
        MessageInformation info1 = createMessageInformation("TASK1", "REQUEST1", "simple request", "any response is valid");
        MessageInformation info2 = createMessageInformation("TASK2", "REQUEST2", "simple request", "any response is valid");
        MessageInformation info3 = createMessageInformation("TASK3", "REQUEST3", "simple request", "any response is valid");

        collection.insert(dao.serializeMessageInfo(info1));
        collection.insert(dao.serializeMessageInfo(info2));
        collection.insert(dao.serializeMessageInfo(info3));

        assertEquals("Retrieved message info does not match the required!", info1, dao.find(info1.getKey()));
        assertEquals("Retrieved message info does not match the required!", info2, dao.find(info2.getKey()));
        assertEquals("Retrieved message info does not match the required!", info3, dao.find(info3.getKey()));

        assertNull("Retrieved message info is not null but should not exist!", dao.find(
                new MessageInformation.Key("TASK", "REQUEST")));
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
}