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
package at.ac.tuwien.dsg.smartcom.manager.messaging.logging;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.manager.messaging.logging.dao.LoggingDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import at.ac.tuwien.dsg.smartcom.utils.PicoHelper;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LoggingServiceTest {

    private PicoHelper pico;
    private SimpleMessageBroker broker;
    private SimpleLoggingDAO dao;

    @Before
    public void setUp() throws Exception {
        pico = new PicoHelper();
        pico.addComponent(SimpleMessageBroker.class);
        pico.addComponent(new SimpleLoggingDAO());
        pico.addComponent(LoggingService.class);
        pico.addComponent(StatisticBean.class);

        broker = pico.getComponent(SimpleMessageBroker.class);
        dao = pico.getComponent(SimpleLoggingDAO.class);

        pico.start();
    }

    @After
    public void tearDown() throws Exception {
        pico.stop();
    }

    @Test
    public void testLogging() throws Exception {
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

        long counter_before = dao.size();

        broker.publishLog(message);

        synchronized (this) {
            wait(1000);
        }

        long counter_after = dao.size();

        assertThat(counter_before, Matchers.lessThan(counter_after));

        for (Message msg : dao.getMessages()) {
            if ("testId".equals(msg.getId().getId())) {
                assertEquals(message, msg);
            }
        }

    }

    private class SimpleLoggingDAO implements LoggingDAO {

        List<Message> messages = new ArrayList<>();

        @Override
        public void persist(Message message) {
            messages.add(message);
        }

        public long size() {
            return messages.size();
        }

        public List<Message> getMessages() {
            return Collections.unmodifiableList(messages);
        }
    }
}