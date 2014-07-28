package at.ac.tuwien.dsg.smartcom.messaging.logging;

import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.messaging.logging.dao.LoggingDAO;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.picocontainer.annotations.Inject;

import javax.annotation.PostConstruct;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class LoggingService implements MessageListener {

    @Inject
    private LoggingDAO dao;

    @Inject
    private MessageBroker broker;

    @PostConstruct
    public void init() {
        broker.registerLogListener(this);
    }

    @Override
    public void onMessage(Message message) {
        dao.persist(message);
    }
}
