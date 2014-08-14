package at.ac.tuwien.dsg.smartcom.utils;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public final class PredefinedMessageHelper {
    private static final Identifier authenticationManager = Identifier.component("AuthenticationManager");

    private static final String AUTH_TYPE = "AUTH";

    private static final String CONTROL_TYPE = "CONTROL";
    private static final String ACK_SUBTYPE = "ACK";
    private static final String COMERROR_SUBTYPE = "COMERROR";
    private static final String TIMEOUT_SUBTYPE = "TIMEOUT";

    private static final String REQUEST_SUBTYPE = "REQUEST";
    private static final String REPLY_SUBTYPE = "REPLY";
    private static final String FAILED_SUBTYPE = "FAILED";
    private static final String ERROR_SUBTYPE = "ERROR";

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
                .setConversationId(message.getConversationId())
                .setType(CONTROL_TYPE)
                .setSubtype(ACK_SUBTYPE)
                .create();
    }

    public static Message createErrorMessage(Message message, String error) {
        return new Message.MessageBuilder()
                .setSenderId(message.getReceiverId())
                .setConversationId(message.getConversationId())
                .setContent(error)
                .setType(CONTROL_TYPE)
                .setSubtype(ERROR_SUBTYPE)
                .create();
    }

    public static Message createCommunicationErrorMessage(Message message, String error) {
        return new Message.MessageBuilder()
                .setSenderId(message.getReceiverId())
                .setConversationId(message.getConversationId())
                .setContent(error)
                .setType(CONTROL_TYPE)
                .setSubtype(COMERROR_SUBTYPE)
                .create();
    }

    public static Message createTimeoutMessage(Message message, String error) {
        return new Message.MessageBuilder()
                .setSenderId(message.getReceiverId())
                .setConversationId(message.getConversationId())
                .setContent(error)
                .setType(CONTROL_TYPE)
                .setSubtype(TIMEOUT_SUBTYPE)
                .create();
    }
}
