package at.ac.tuwien.dsg.smartcom.utils;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @author Ognjen Scekic
 * @version 1.1
 */
public final class PredefinedMessageHelper {
    public static final Identifier authenticationManager = Identifier.component("AuthenticationManager");
    public static final Identifier taskExecutionEngine = Identifier.component("TaskExecutionEngine");

    public static final String AUTH_TYPE = "AUTH";
    public static final String CONTROL_TYPE = "CONTROL";
    public static final String DATA_TYPE = "DATA";
    
    
    public static final String ACK_SUBTYPE = "ACK";
    public static final String ACK_SUBTYPE_CHECKED = "ACK_CHECKED";
    public static final String COMERROR_SUBTYPE = "COMERROR";
    public static final String DELIVERY_ERROR_SUBTYPE = "DELIVERYERROR";
    public static final String TIMEOUT_SUBTYPE = "TIMEOUT";

    public static final String REQUEST_SUBTYPE = "REQUEST";
    public static final String REPLY_SUBTYPE = "REPLY";
    public static final String FAILED_SUBTYPE = "FAILED";
    public static final String ERROR_SUBTYPE = "ERROR";

    public static Message createAuthenticationRequestMessage(Identifier sender, String password) {
        return new Message.MessageBuilder()
                .setSenderId(sender)
                .setContent(password)
                .setReceiverId(authenticationManager)
                .setType(AUTH_TYPE)
                .setSubtype(REQUEST_SUBTYPE)
                .create();
    }

    public static Message createAuthenticationSuccessfulMessage(Identifier receiver, String token) {
        return new Message.MessageBuilder()
                .setReceiverId(receiver)
                .setContent(token)
                .setSenderId(authenticationManager)
                .setType(AUTH_TYPE)
                .setSubtype(REPLY_SUBTYPE)
                .create();
    }

    public static Message createAuthenticationFailedMessage(Identifier receiver) {
        return new Message.MessageBuilder()
                .setReceiverId(receiver)
                .setSenderId(authenticationManager)
                .setType(AUTH_TYPE)
                .setSubtype(FAILED_SUBTYPE)
                .create();
    }

    public static Message createAuthenticationErrorMessage(Identifier receiver, String message) {
        return new Message.MessageBuilder()
                .setReceiverId(receiver)
                .setContent("An error occurred during the authentication: "+message)
                .setSenderId(authenticationManager)
                .setType(AUTH_TYPE)
                .setSubtype("ERROR")
                .create();
    }

    public static Message createAcknowledgeMessage(Message message) {
        return new Message.MessageBuilder()
                .setSenderId(message.getReceiverId())
                .setReceiverId(message.getSenderId())
                .setConversationId(message.getConversationId())
                .setRefersTo(message.getId())
                .setType(CONTROL_TYPE)
                .setSubtype(ACK_SUBTYPE)
                .create();
    }

    public static Message createErrorMessage(Message message, String error) {
        return new Message.MessageBuilder()
                .setSenderId(message.getReceiverId())
                .setConversationId(message.getConversationId())
                .setRefersTo(message.getId())
                .setContent(error)
                .setType(CONTROL_TYPE)
                .setSubtype(ERROR_SUBTYPE)
                .create();
    }

    public static Message createCommunicationErrorMessage(Message message, String error) {
        return new Message.MessageBuilder()
                .setSenderId(message.getReceiverId())
                .setReceiverId(message.getSenderId())
                .setConversationId(message.getConversationId())
                .setRefersTo(message.getId())
                .setContent(error)
                .setType(CONTROL_TYPE)
                .setSubtype(COMERROR_SUBTYPE)
                .create();
    }
    
    /**
     * Creates a new delivery error message based on the message passed as input parameter. 
     * @param message that will be wrapped around with the newly created error message. The fields refersTo of the new message will contain this message's ID. 
     * @param error String that will be set as the contents of the new message
     * @param sender Identifier that will be set as the sender.
     * @return newly created message
     */
    public static Message createDeliveryErrorMessage(Message message, String error, Identifier sender) {
        return new Message.MessageBuilder()
                .setSenderId(sender)
                .setReceiverId(null) //to differentiate from real messages in messageHandle, e.g., to leave isPrimaryRecipient false, and to allow determineReceivers() to handle it properly
                .setConversationId(message.getConversationId())
                .setRefersTo(message.getId())
                .setContent(error)
                .setType(CONTROL_TYPE)
                .setSubtype(DELIVERY_ERROR_SUBTYPE)
                .create();
    }
    
    
    /**
     * Should be called after the original ACK/ERR from the adapter was processed and the corresponding structures
     * updated. It will take the ID of that original message, but that message should not be sent to send() but rather to
     * handleMessage() directly, as the receiver will be null, so its actual recipient should be determined from
     * @param message
     * @return
     */
    public static Message createDeliveryErrorMessageFromAdaptersCommunicationErrorMessage(Message message) {    	
    	return new Message.MessageBuilder()
                .setReceiverId(null) //to differentiate from real messages in messageHandle, e.g., to leave isPrimaryRecipient false, and to allow determineReceivers() to handle it properly
                .setConversationId(message.getConversationId())
                .setRefersTo(message.getRefersTo()) //refersTo should already be correctly set
                .setContent(message.getContent())
                
                .setId(message.getId()) //we take the id of the original message. This means it must not go through send() or it will get discarded
                
                .setType(CONTROL_TYPE)
                .setSubtype(DELIVERY_ERROR_SUBTYPE)
                .create();
    }
    
    /**
     * Should be called after the original ACK/ERR from the adapter was processed and the corresponding structures
     * updated. It will take the ID of that original message, but that message should not be sent to send() but rather to
     * handleMessage() directly, as the receiver will be null, so its actual recipient should be determined from
     * @param message
     * @return
     */
    public static Message createAcknowledgeMessageFromAdaptersAcknowledgeMessage(Message message) {
    	return new Message.MessageBuilder()
        .setReceiverId(null) //to differentiate from real messages in messageHandle, e.g., to leave isPrimaryRecipient false, and to allow determineReceivers() to handle it properly
        .setConversationId(message.getConversationId())
        .setRefersTo(message.getRefersTo()) //refersTo should already be correctly set
        .setContent(message.getContent())
        
        .setId(message.getId()) //we take the id of the original message. This means it must not go through send() or it will get discarded
        
        .setType(CONTROL_TYPE)
        .setSubtype(ACK_SUBTYPE_CHECKED)
        .create();
    }

    public static Message createTimeoutMessage(Message message, String error) {
        return new Message.MessageBuilder()
                .setSenderId(message.getReceiverId())
                .setConversationId(message.getConversationId())
                .setRefersTo(message.getId())
                .setContent(error)
                .setType(CONTROL_TYPE)
                .setSubtype(TIMEOUT_SUBTYPE)
                .create();
    }
}
