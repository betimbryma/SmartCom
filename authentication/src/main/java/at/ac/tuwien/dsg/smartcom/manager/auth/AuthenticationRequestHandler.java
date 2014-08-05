package at.ac.tuwien.dsg.smartcom.manager.auth;

import at.ac.tuwien.dsg.smartcom.broker.CancelableListener;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.manager.auth.dao.AuthenticationSessionDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Calendar;
import java.util.UUID;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class AuthenticationRequestHandler implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationRequestHandler.class);

    public static final int DEFAULT_SESSION_VALIDITY_IN_MINUTES = 10;

    @Inject
    private AuthenticationSessionDAO dao;

    @Inject
    private MessageBroker broker;

    @Inject
    private PMCallback callback;

    private CancelableListener listenerRegistration;

    @PostConstruct
    public void init() {
        listenerRegistration = broker.registerAuthListener(this);
    }

    @PreDestroy
    public void preDestroy() {
        listenerRegistration.cancel();
    }

    @Override
    public void onMessage(Message message) {
        log.debug("No authentication request: {}", message);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, DEFAULT_SESSION_VALIDITY_IN_MINUTES);

        Message msg;
        Message.MessageBuilder builder = new Message.MessageBuilder()
                .setReceiverId(message.getSenderId())
                .setSenderId(Identifier.component("AuthenticationManager"))
                .setType("AUTH");
        try {
            if (callback.authenticate(message.getSenderId(), message.getContent())) {
                String session = createSessionId();
                dao.persistSession(message.getSenderId(), session, cal.getTime());
                log.debug("Created session with token {} for peer {}", session, message.getSenderId());

                //Transfer the session id to the sender
                msg = builder
                        .setContent(session)
                        .setSubtype("REPLY")
                        .create();
            } else {
                //Tell the sender that his request was not valid
                msg = builder
                        .setSubtype("FAILED")
                        .create();
            }
        } catch (PeerAuthenticationException e) {
            log.error("An error occurred during the authentication", e);

            //Tell the sander that there was an error
            msg = builder
                    .setContent("An error occurred during the authentication: "+e.getLocalizedMessage())
                    .setSubtype("ERROR")
                    .create();
        }

        broker.publishControl(msg);
    }

    private String createSessionId() {
        return UUID.randomUUID().toString();
    }
}
