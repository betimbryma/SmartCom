package at.ac.tuwien.dsg.smartcom.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import at.ac.tuwien.dsg.smartcom.utils.PropertiesLoader;
import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Adapter(name = "Android", stateful = false)
public class AndroidOutputAdapter implements OutputAdapter {
    private static final Logger log = LoggerFactory.getLogger(AndroidOutputAdapter.class);

    private final Sender sender;

    public AndroidOutputAdapter() {
        sender = new Sender(PropertiesLoader.getProperty("AndroidAdapter.properties", "key"));
    }

    @Override
    public void push(Message message, PeerAddress address) throws AdapterException {
        com.google.android.gcm.server.Message msg = new com.google.android.gcm.server.Message.Builder().addData("message", message.getContent()).build();

        Result result;
        String regId = (String) address.getContactParameters().get(0);
        try {
            log.debug("Sending message using GoogleCloudMessaging to peer {}", address.getPeerId());
            result = sender.send(msg, regId, 5);
        } catch (IOException e) {
            log.error("Could not send push notification via GoogleCloudMessaging to peer {}", address.getPeerId());
            throw new AdapterException("Could not send push notification via GoogleCloudMessaging!");
        }

        // analyze the results
        String messageId = result.getMessageId();
        if (messageId != null) {
            log.debug("Successfully sent message to device {}. MessageId = {}", regId, messageId);
        } else {
            String error = result.getErrorCodeName();
            if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                // application has been removed from device...
                log.error("Could not send message to peer {} because the provided ID for the GoogleCloudMessaging of the peer is not valid anymore!", address.getPeerId());
                throw new AdapterException("Could not send message because the provided ID is not valid anymore!");
            } else {
                log.error("Error sending message to {}: {}", address.getPeerId(), error);
                throw new AdapterException("Error sending a message: "+error);
            }
        }


    }
}
