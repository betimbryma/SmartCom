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
package at.ac.tuwien.dsg.smartcom.manager.messaging.logging.dao;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.IdentifierType;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MongoDBLoggingDAOTest {

    private MongoDBInstance mongoDB;

    MongoDBLoggingDAO logger;
    DBCollection collection;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();


        MongoClient mongo = mongoDB.getClient();
        collection = mongo.getDB("test-resolver").getCollection("resolver");
        logger = new MongoDBLoggingDAO(mongo, "test-resolver", "resolver");
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
    }

    @Test
    public void testPersist() throws Exception {
        Message message = new Message.MessageBuilder()
                .setId(Identifier.message("testId"))
                .setContent("testContent")
                .setType("testType")
                .setSubtype("testSubType")
                .setSenderId(Identifier.peer("sender"))
                .setReceiverId(Identifier.peer("receiver"))
                .setConversationId("conversationId")
                .setTtl(3)
                .setLanguage("testLanguage")
                .setSecurityToken("securityToken")
                .create();

        long counter_before = collection.count();

        logger.persist(message);

        long counter_after = collection.count();

        assertThat(counter_before, Matchers.lessThan(counter_after));

        for (DBObject dbObject : collection.find()) {
            if ("testId".equals(dbObject.get("_id"))) {
                assertEquals("testContent", dbObject.get("content"));
                assertEquals("testType", dbObject.get("type"));
                assertEquals("testSubType", dbObject.get("subtype"));
                assertEquals(IdentifierType.PEER, IdentifierType.valueOf((String) ((DBObject) dbObject.get("sender")).get("type")));
                assertEquals("sender", ((DBObject)dbObject.get("sender")).get("id"));
                assertEquals(IdentifierType.PEER, IdentifierType.valueOf((String) ((DBObject) dbObject.get("receiver")).get("type")));
                assertEquals("receiver", ((DBObject)dbObject.get("receiver")).get("id"));
                assertEquals("conversationId", dbObject.get("conversationId"));
                assertEquals(3l, dbObject.get("ttl"));
                assertEquals("testLanguage", dbObject.get("language"));
                assertEquals("securityToken", dbObject.get("securityToken"));
            }
        }

    }
}