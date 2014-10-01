package at.ac.tuwien.dsg.smartcom.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.adapters.email.MailUtils;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Adapter(name = "Email", stateful = false)
public class EmailOutputAdapter implements OutputAdapter {
    private static final Logger log = LoggerFactory.getLogger(EmailOutputAdapter.class);

    @Override
    public void push(Message message, PeerChannelAddress address) throws AdapterException {
        if (address.getContactParameters().size() == 0) {
            log.error("Peer address does not provide the required email address!");
            throw new AdapterException();
        }

        String recipient = (String) address.getContactParameters().get(0);

        try {
            MailUtils.sendMail(recipient, message.getConversationId(), message.getContent());
        } catch (MessagingException e) {
            throw new AdapterException(e);
        }
    }
}
