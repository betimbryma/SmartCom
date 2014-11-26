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

import at.ac.tuwien.dsg.smartcom.broker.*;
import at.ac.tuwien.dsg.smartcom.broker.policy.DynamicReplicationPolicy;
import at.ac.tuwien.dsg.smartcom.manager.messaging.logging.dao.LoggingDAO;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import org.picocontainer.annotations.Inject;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class LoggingService implements MessageListener {

    @Inject
    private LoggingDAO dao;

    @Inject
    private MessageBroker broker;

    @Inject
    private StatisticBean statistic;

    private CancelableListener registration;
    private ReplicatingMessageListener listener;

    @PostConstruct
    public void init() {
        listener = new ReplicatingMessageListener("log", this, new ReplicationFactory() {
            @Override
            public MessageListener createReplication() {
                return LoggingService.this;
            }
        }, new DynamicReplicationPolicy());
        registration = broker.registerLogListener(listener);
    }

    @PreDestroy
    public void preDestroy() {
        registration.cancel();
        listener.shutdown();
    }

    @Override
    public void onMessage(Message message) {
        statistic.logReceived();
        dao.persist(message);
    }
}
