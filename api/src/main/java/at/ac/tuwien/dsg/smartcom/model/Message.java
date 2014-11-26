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
    private DeliveryPolicy.Message delivery = DeliveryPolicy.Message.UNACKNOWLEDGED;
    
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

    public DeliveryPolicy.Message getDelivery() {
        return delivery;
    }

    public void setDelivery(DeliveryPolicy.Message delivery) {
        this.delivery = delivery;
    }

    @Override
    public Message clone() {
        Message msg = new Message();
        if (this.id != null) {
            msg.id = this.id.clone();
        }
        msg.content = this.content;
        msg.type = this.type;
        msg.subtype = this.subtype;
        if (senderId != null) {
            msg.senderId = this.senderId.clone();
        }
        if (receiverId != null) {
            msg.receiverId = this.receiverId.clone();
        }
        msg.conversationId = this.conversationId;
        msg.ttl = this.ttl;
        msg.language = this.language;
        msg.securityToken = this.securityToken;
        msg.delivery = this.delivery;
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
                ", delivery='" + delivery + "\'" +
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

        public MessageBuilder setDeliveryPolicy(DeliveryPolicy.Message deliveryPolicy) {
            msg.delivery = deliveryPolicy;
            return this;
        }

        public Message create() {
            Message message = msg;
            msg = new Message();
            return message;
        }
    }
}
