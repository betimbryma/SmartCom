package at.ac.tuwien.dsg.smartcom.model;

import java.io.Serializable;

/**
 * This class represents a message that will be sent to peers
 * or has been received from external communication channels.
 *
 * Furthermore this class can be used for internal messages too.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @author Ognjen Scekic
 * @version 1.1
 */
public class Message implements Serializable, Cloneable {

    private Identifier id;
    private String content;
    private String type;
    private String subtype;
    private Identifier senderId;
    private Identifier receiverId;
    private String conversationId;
    private long ttl;
    private String language;
    private String securityToken;
    private boolean wantsAcknowledgement = false;
    
    private Identifier refersTo; //in case of a control message, reporting failed delivery message, or ACK, this field indicates the Identifier of the original message that the control message refers to. 

    public Identifier getId() {
        return id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public Identifier getSenderId() {
        return senderId;
    }

    public void setSenderId(Identifier senderId) {
        this.senderId = senderId;
    }

    public Identifier getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Identifier receiverId) {
        this.receiverId = receiverId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }
    
    public Identifier getRefersTo() {
		return refersTo;
	}

	public void setRefersTo(Identifier refersTo) {
		this.refersTo = refersTo;
	}

    public boolean isWantsAcknowledgement() {
        return wantsAcknowledgement;
    }

    public void setWantsAcknowledgement(boolean wantsAcknowledgement) {
        this.wantsAcknowledgement = wantsAcknowledgement;
    }

    @Override
    public Message clone() {
        Message msg = new Message();
        if (this.id != null) {
            msg.id = new Identifier(this.id.getType(), this.id.getId(), this.id.getPostfix());
        }
        msg.content = this.content;
        msg.type = this.type;
        msg.subtype = this.subtype;
        if (senderId != null) {
            msg.senderId = new Identifier(this.senderId.getType(), this.senderId.getId(), this.senderId.getPostfix());
        }
        if (receiverId != null) {
            msg.receiverId = new Identifier(this.receiverId.getType(), this.receiverId.getId(), this.receiverId.getPostfix());
        }
        msg.conversationId = this.conversationId;
        msg.ttl = this.ttl;
        msg.language = this.language;
        msg.securityToken = this.securityToken;
        msg.wantsAcknowledgement = this.wantsAcknowledgement;
        return msg;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id ='" + id + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", subtype='" + subtype + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", ttl=" + ttl +
                ", language='" + language + '\'' +
                ", securityToken='" + securityToken + '\'' +
                ", refersTo='" + refersTo + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (id != null ? !id.equals(message.id) : message.id != null) return false;
        if (subtype != null ? !subtype.equals(message.subtype) : message.subtype != null) return false;
        if (type != null ? !type.equals(message.type) : message.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (subtype != null ? subtype.hashCode() : 0);
        return result;
    }

    public static class MessageBuilder {
        private Message msg = new Message();

        public MessageBuilder setId(Identifier id) {
            msg.id = id;
            return this;
        }

        public MessageBuilder setContent(String content) {
            msg.content = content;
            return this;
        }

        public MessageBuilder setType(String type) {
            msg.type = type;
            return this;
        }

        public MessageBuilder setSubtype(String subtype) {
            msg.subtype = subtype;
            return this;
        }

        public MessageBuilder setSenderId(Identifier senderId) {
            msg.senderId = senderId;
            return this;
        }

        public MessageBuilder setReceiverId(Identifier receiverId) {
            msg.receiverId = receiverId;
            return this;
        }

        public MessageBuilder setConversationId(String conversationId) {
            msg.conversationId = conversationId;
            return this;
        }

        public MessageBuilder setTtl(long ttl) {
            msg.ttl = ttl;
            return this;
        }

        public MessageBuilder setLanguage(String language) {
            msg.language = language;
            return this;
        }

        public MessageBuilder setSecurityToken(String securityToken) {
            msg.securityToken = securityToken;
            return this;
        }
        
        public MessageBuilder setRefersTo(Identifier refersTo) {
            msg.refersTo = refersTo;
            return this;
        }

        public MessageBuilder setWantsAcknowledgement(boolean acknowledgement) {
            msg.wantsAcknowledgement = acknowledgement;
            return this;
        }

        public Message create() {
            Message message = msg;
            msg = new Message();
            return message;
        }
    }
}
