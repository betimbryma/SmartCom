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
package at.ac.tuwien.dsg.smartcom.utils;

import at.ac.tuwien.dsg.smartcom.exception.IllegalQueryException;
import at.ac.tuwien.dsg.smartcom.manager.messaging.logging.dao.MongoDBLoggingDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.QueryCriteria;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public abstract class MessageQueryTestClass {

    protected MongoDBLoggingDAO logger;
    protected MongoDBInstance mongoDB;

    public abstract QueryCriteria createCriteria();

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        logger = new MongoDBLoggingDAO(mongoDB.getClient(), "test-log", "log");
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
    }

    @Test
    public void testQuery_simpleQueries() throws Exception {
        Date before = new Date();
        int i = 0;
        i += addTestData(i);

        Date after;
        synchronized (this) {
            wait(10);
            after = new Date();
            wait(10);
        }

        //there is only one message with the given id
        QueryCriteria criteria = createCriteria();
        criteria.id(Identifier.message("id0"));
        Collection<Message> queryResult = criteria.query();
        assertThat(queryResult, Matchers.hasSize(1));
        assertEquals("id0", queryResult.iterator().next().getId().getId());

        //3 messages have been created after starting the test
        criteria = createCriteria();
        criteria.created(before, null);
        assertThat(criteria.query(), Matchers.hasSize(3));

        //no messages have been created after adding the test data
        criteria = createCriteria();
        criteria.created(after, null);
        assertThat(criteria.query(), Matchers.empty());

        //add more test data (3 messages)
        addTestData(i);

        //after the first 3 messages there have been another 3 messages
        criteria = createCriteria();
        criteria.created(after, null);
        assertThat(criteria.query(), Matchers.hasSize(3));

        //in this period the first 3 messages have been created
        criteria = createCriteria();
        criteria.created(before, after);
        assertThat(criteria.query(), Matchers.hasSize(3));

        //empty query should return everything
        criteria = createCriteria();
        assertThat(criteria.query(), Matchers.hasSize(6));

        //there is only one message with that type
        criteria = createCriteria();
        criteria.type("type4");
        queryResult = criteria.query();
        assertThat(queryResult, Matchers.hasSize(1));
        assertEquals("type4", queryResult.iterator().next().getType());

        //there is only one message with that subtype
        criteria = createCriteria();
        criteria.subtype("subtype5");
        queryResult = criteria.query();
        assertThat(queryResult, Matchers.hasSize(1));
        assertEquals("subtype5", queryResult.iterator().next().getSubtype());

        //there is only one message with that sender
        criteria = createCriteria();
        criteria.from(Identifier.peer("sender1"));
        queryResult = criteria.query();
        assertThat(queryResult, Matchers.hasSize(1));
        assertEquals("sender1", queryResult.iterator().next().getSenderId().getId());

        //there is only one message with that receiver
        criteria = createCriteria();
        criteria.to(Identifier.peer("receiver2"));
        queryResult = criteria.query();
        assertThat(queryResult, Matchers.hasSize(1));
        assertEquals("receiver2", queryResult.iterator().next().getReceiverId().getId());

        //there is only one message with that conversation id
        criteria = createCriteria();
        criteria.conversationId("conversationId4");
        queryResult = criteria.query();
        assertThat(queryResult, Matchers.hasSize(1));
        assertEquals("conversationId4", queryResult.iterator().next().getConversationId());
    }

    @Test(expected = IllegalQueryException.class)
    public void testQuery_exceptionQuery() throws Exception {
        Date before = new Date();
        int i = 0;
        i += addTestData(i);
        Date after = new Date();

        QueryCriteria criteria = createCriteria();
        criteria.created(after, before);
        criteria.query(); //should throw an exception, because the second parameter is before the first
    }

    @Test
    public void testQuery_complexQueries() throws Exception {
        for (int i = 0; i < 200; i++) {
            createMessage2(i);
        }

        Date before;
        synchronized (this) {
            wait(10);
            before = new Date();
            wait(10);
        }

        for (int i = 200; i < 1000; i++) {
            createMessage2(i);
        }

        Date after;
        synchronized (this) {
            wait(10);
            after = new Date();
            wait(10);
        }

        for (int i = 1000; i < 1200; i++) {
            createMessage2(i);
        }


        QueryCriteria criteria = createCriteria();
        assertThat(criteria.query(), Matchers.hasSize(1200));

        criteria.created(before, null);
        assertThat(criteria.query(), Matchers.hasSize(1000));

        criteria.created(before, after);
        assertThat(criteria.query(), Matchers.hasSize(800));

        criteria.created(before, after);
        criteria.conversationId("conversationId1");
        assertThat(criteria.query(), Matchers.hasSize(400));

        criteria.created(before, after);
        criteria.conversationId("conversationId1");
        criteria.to(Identifier.peer("receiver2"));
        assertThat(criteria.query(), Matchers.hasSize(133));

        criteria.created(before, after);
        criteria.conversationId("conversationId1");
        criteria.to(Identifier.peer("receiver2"));
        criteria.from(Identifier.peer("sender3"));
        assertThat(criteria.query(), Matchers.hasSize(67));

        criteria.created(before, after);
        criteria.conversationId("conversationId1");
        criteria.to(Identifier.peer("receiver2"));
        criteria.from(Identifier.peer("sender3"));
        criteria.subtype("subtype4");
        assertThat(criteria.query(), Matchers.hasSize(13));

        criteria.created(before, after);
        criteria.conversationId("conversationId1");
        criteria.to(Identifier.peer("receiver2"));
        criteria.from(Identifier.peer("sender3"));
        criteria.subtype("subtype4");
        criteria.type("type1");
        System.out.println("Messages:");
        assertThat(criteria.query(), Matchers.hasSize(2));

        criteria.created(before, after);
        criteria.conversationId("conversationId1");
        criteria.to(Identifier.peer("receiver2"));
        criteria.from(Identifier.peer("sender3"));
        criteria.subtype("subtype4");
        criteria.type("type1");
        criteria.id(Identifier.message("id659"));
        assertThat(criteria.query(), Matchers.hasSize(1));
    }

    private void createMessage2(int i) {
        logger.persist(new Message.MessageBuilder()
                .setId(Identifier.message("id" + i))
                .setContent("content" + i)
                .setType("type" + (i % 7))
                .setSubtype("subtype" + (i % 5))
                .setSenderId(Identifier.peer("sender" + (i % 4)))
                .setReceiverId(Identifier.peer("receiver" + (i % 3)))
                .setConversationId("conversationId" + (i % 2))
                .setTtl(3 + i)
                .setLanguage("language" + i)
                .setSecurityToken("securityToken" + i)
                .create());
    }

    private int addTestData(int i) {
        logger.persist(createMessage(i++));
        logger.persist(createMessage(i++));
        logger.persist(createMessage(i++));
        return i;
    }

    private Message createMessage(int i) {
        return new Message.MessageBuilder()
                .setId(Identifier.message("id"+i))
                .setContent("content"+i)
                .setType("type"+i)
                .setSubtype("subtype"+i)
                .setSenderId(Identifier.peer("sender"+i))
                .setReceiverId(Identifier.peer("receiver"+i))
                .setConversationId("conversationId"+i)
                .setTtl(3+i)
                .setLanguage("language"+i)
                .setSecurityToken("securityToken"+i)
                .create();
    }

}
