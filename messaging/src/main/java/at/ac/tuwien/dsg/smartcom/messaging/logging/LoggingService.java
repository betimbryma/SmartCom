package at.ac.tuwien.dsg.smartcom.messaging.logging;

import at.ac.tuwien.dsg.smartcom.broker.*;
import at.ac.tuwien.dsg.smartcom.broker.policy.DynamicReplicationPolicy;
import at.ac.tuwien.dsg.smartcom.messaging.logging.dao.LoggingDAO;
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
