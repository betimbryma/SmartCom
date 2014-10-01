package at.ac.tuwien.dsg.smartcom.rest.model;

import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 *   {
 *    "id": {
 *        "type": "message",
 *        "id": "1"
 *    },
 *    "content": "test",
 *    "type": "test",
 *    "subtype": "test",
 *    "senderId": {
 *        "type": "peer",
 *        "id": "peer1"
 *    },
 *    "receiverId": {
 *        "type": "peer",
 *        "id": "humanPeer1"
 *    },
 *    "conversationId": "test",
 *    "ttl": 1,
 *    "language": "test",
 *    "securityToken": "asdf",
 *    "wantsAcknowledgement": true
 *   }
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MessageDTO {
    private IdentifierDTO id;
    private String content;
    private String type;
    private String subtype;
    private IdentifierDTO senderId;
    private IdentifierDTO receiverId;
    private String conversationId;
    private long ttl;
    private String language;
    private String securityToken;
    private boolean wantsAcknowledgement = false;

    private IdentifierDTO refersTo; //in case of a control message, reporting failed delivery message, or ACK, this field indicates the Identifier of the original message that the control message refers to.

    public MessageDTO() {
    }

    public MessageDTO(Message message) {
        this.ttl = message.getTtl();
        this.language = message.getLanguage();
        this.securityToken = message.getSecurityToken();
        this.wantsAcknowledgement = message.isWantsAcknowledgement();
        this.content = message.getContent();
        this.type = message.getType();
        this.subtype = message.getSubtype();
        this.conversationId = message.getConversationId();

        if (message.getId() != null) {
            this.id = new IdentifierDTO(message.getId());
        }

        if (message.getSenderId() != null) {
            this.senderId = new IdentifierDTO(message.getSenderId());
        }

        if (message.getReceiverId() != null) {
            this.receiverId = new IdentifierDTO(message.getReceiverId());
        }

        if (message.getRefersTo() != null) {
            this.refersTo = new IdentifierDTO(message.getRefersTo());
        }
    }

    public Message create() {
        Message.MessageBuilder builder = new Message.MessageBuilder();
        builder = builder.setTtl(this.ttl);
        builder = builder.setLanguage(this.language);
        builder = builder.setSecurityToken(this.securityToken);
        builder = builder.setWantsAcknowledgement(this.wantsAcknowledgement);
        builder = builder.setContent(this.content);
        builder = builder.setType(this.type);
        builder = builder.setSubtype(this.subtype);
        builder = builder.setConversationId(this.conversationId);

        if (getId() != null) {
            builder.setId(getId().create());
        }

        if (getSenderId() != null) {
            builder.setSenderId(getSenderId().create());
        }

        if (getReceiverId() != null) {
            builder.setReceiverId(getReceiverId().create());
        }

        if (getRefersTo() != null) {
            builder.setRefersTo(getRefersTo().create());
        }

        return builder.create();
    }

    public IdentifierDTO getId() {
        return id;
    }

    public void setId(IdentifierDTO id) {
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

    public IdentifierDTO getSenderId() {
        return senderId;
    }

    public void setSenderId(IdentifierDTO senderId) {
        this.senderId = senderId;
    }

    public IdentifierDTO getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(IdentifierDTO receiverId) {
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

    public IdentifierDTO getRefersTo() {
        return refersTo;
    }

    public void setRefersTo(IdentifierDTO refersTo) {
        this.refersTo = refersTo;
    }

    public boolean isWantsAcknowledgement() {
        return wantsAcknowledgement;
    }

    public void setWantsAcknowledgement(boolean wantsAcknowledgement) {
        this.wantsAcknowledgement = wantsAcknowledgement;
    }

    @Override
    public String toString() {
        return "MessageDTO{" +
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

        MessageDTO message = (MessageDTO) o;

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

}
