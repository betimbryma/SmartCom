package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.exception.UnknownMessageException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.MessageInformation;

/**
 * The message info service provides information on the semantics of messages,
 * how to interpret them in a human-readable way and which messages are related
 * to a message. Therefore it provides methods to query message information and
 * to add additional information to messages.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessageInfoService {

    /**
     * Returns information on a given message to the caller.
     *
     * @param message Can be a valid message ID or an instance of Message.
     * @return Returns the information for a given message
     * @throws UnknownMessageException no message of that type found or the MessageId is not valid.
     */
    public MessageInformation getInfoForMessage(Message message) throws UnknownMessageException;

    /**
     * Add information on a given message. If there is already exists information for a message,
     * it will be replaced by this one.
     *
     * @param message Specifies the type of message.
     * @param info Information for messages of the type of parameter message.
     */
    public void addMessageInfo(Message message, MessageInformation info);
}
