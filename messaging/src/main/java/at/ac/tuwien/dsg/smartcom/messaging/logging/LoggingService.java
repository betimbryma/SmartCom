package at.ac.tuwien.dsg.smartcom.messaging.logging;

import at.ac.tuwien.dsg.smartcom.broker.CancelableListener;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.messaging.logging.dao.LoggingDAO;
import at.ac.tuwien.dsg.smartcom.model.Message;
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
    private CancelableListener registration;

    @PostConstruct
    public void init() {
        registration = broker.registerLogListener(this);
    }

    @PreDestroy
    public void preDestroy() {
        registration.cancel();
    }

    @Override
    public void onMessage(Message message) {
        dao.persist(message);
    }
}
