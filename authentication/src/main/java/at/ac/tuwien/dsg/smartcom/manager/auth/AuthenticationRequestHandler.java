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
package at.ac.tuwien.dsg.smartcom.manager.auth;

import at.ac.tuwien.dsg.smartcom.broker.CancelableListener;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.callback.PeerAuthenticationCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.manager.auth.dao.AuthenticationSessionDAO;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.utils.PredefinedMessageHelper;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Calendar;
import java.util.UUID;

/**
 * Handler that waits for authentication requests and handles them properly. Therefore
 * it registers itself at the message broker as a listener for authentication messages.
 * Upon reception of such a message, the handler will check if the provided credentials
 * are correct by asking the PMCallback. If they are correct, the handler will create
 * a new security token that can be used by a peer for a predefined time.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class AuthenticationRequestHandler implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationRequestHandler.class);

    public static final int DEFAULT_SESSION_VALIDITY_IN_MINUTES = 10;

    @Inject
    private AuthenticationSessionDAO dao; //used to persist the newly created session

    @Inject
    private MessageBroker broker; //used to register itself as a listener and to send respond messages

    @Inject
    private PeerAuthenticationCallback callback; //used to check if the credentials are correct

    private CancelableListener listenerRegistration; //used to cancel the registration as listener when shutting down.

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
        try {
            //check if the credentials are correct
            //the sender of the message provides the peer id and the message itself contains the password
            if (callback.authenticate(message.getSenderId(), message.getContent())) {

                //create a new session and store its value in the database
                String session = createSessionId();
                dao.persistSession(message.getSenderId(), session, cal.getTime());
                log.debug("Created session with token {} for peer {}", session, message.getSenderId());

                //Transfer the session id to the sender
                msg = PredefinedMessageHelper.createAuthenticationSuccessfulMessage(message.getSenderId(), session);
            } else {
                //Tell the sender that his request was not valid
                msg = PredefinedMessageHelper.createAuthenticationFailedMessage(message.getSenderId());
            }
        } catch (PeerAuthenticationException e) {
            log.error("An error occurred during the authentication", e);

            //Tell the sander that there was an error
            msg = PredefinedMessageHelper.createAuthenticationErrorMessage(message.getSenderId(), e.getLocalizedMessage());
        }

        broker.publishControl(msg);
    }

    /**
     * Creates a unique session id
     * @return unique session id
     */
    private String createSessionId() {
        return UUID.randomUUID().toString();
    }
}
