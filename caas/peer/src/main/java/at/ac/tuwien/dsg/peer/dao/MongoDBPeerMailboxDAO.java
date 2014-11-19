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
package at.ac.tuwien.dsg.peer.dao;

import at.ac.tuwien.dsg.smartcom.adapters.rest.JsonMessageDTO;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MongoDBPeerMailboxDAO implements PeerMailboxDAO {
    private static final Logger log = LoggerFactory.getLogger(MongoDBPeerMailboxDAO.class);
    private static final String COLLECTION = "PeerMailbox";

    private final DBCollection coll;

    public MongoDBPeerMailboxDAO(String host, int port, String database) throws UnknownHostException {
        this(new MongoClient(host, port), database, COLLECTION);
    }

    public MongoDBPeerMailboxDAO(MongoClient client, String database, String collection) {
        coll = client.getDB(database).getCollection(collection);
    }

    @Override
    public JsonMessageDTO persistMessage(JsonMessageDTO message, String receiver) {
        BasicDBObject dbObject = serializeMessage(message, receiver);
        BasicDBObject update = new BasicDBObject("$push", new BasicDBObject("messages", dbObject));

        WriteResult id = coll.update(new BasicDBObject("_id", receiver), update, true, false);
        return message;
    }

    @Override
    public List<JsonMessageDTO> getMessagesForReceiver(String receiver) {
        DBObject object = coll.findOne(new BasicDBObject("_id", receiver));
        return deserializeList(object);
    }

    @Override
    public JsonMessageDTO pullMessageForReceiver(String receiver) {
        DBObject modify = coll.findAndModify(new BasicDBObject("_id", receiver), new BasicDBObject("$pop", new BasicDBObject("messages", -1)));
        if (modify == null) {
            return null;
        }
        BasicDBList list = (BasicDBList) modify.get("messages");
        if (list.size() == 0) {
            return null;
        }
        return deserialize((DBObject) list.get(0));
    }

    private BasicDBObject serializeMessage(JsonMessageDTO message, String receiver) {
        BasicDBObject object = new BasicDBObject();
        if (message.getId() != null) {
            object = object.append("id", message.getId());
        }
        if (message.getType() != null) {
            object = object.append("type", message.getType());
        }
        if (message.getSubtype() != null) {
            object = object.append("subtype", message.getSubtype());
        }
        if (message.getSender() != null) {
            object = object.append("sender", message.getSender());
        }
        if (message.getContent() != null) {
            object = object.append("content", message.getContent());
        }
        if (message.getConversation() != null) {
            object = object.append("conversationId", message.getConversation());
        }
        if (message.getLanguage() != null) {
            object = object.append("language", message.getLanguage());
        }
        if (message.getSecurityToken() != null) {
            object = object.append("securityToken", message.getSecurityToken());
        }
        return object;
    }

    private List<JsonMessageDTO> deserializeList(DBObject object) {
        if (object == null) {
            return new ArrayList<>();
        }
        BasicDBList list = (BasicDBList) object.get("messages");

        List<JsonMessageDTO> messages = new ArrayList<>(list.size());
        for (Object o : list) {
            messages.add(deserialize((DBObject) o));
        }

        return messages;
    }

    private JsonMessageDTO deserialize(DBObject object) {
        JsonMessageDTO dto = new JsonMessageDTO();
        if (object.containsField("id")) {
            dto.setId((String) object.get("id"));
        }
        if (object.containsField("type")) {
            dto.setType((String) object.get("type"));
        }
        if (object.containsField("subtype")) {
            dto.setSubtype((String) object.get("subtype"));
        }
        if (object.containsField("content")) {
            dto.setContent((String) object.get("content"));
        }
        if (object.containsField("conversationId")) {
            dto.setConversation((String) object.get("conversationId"));
        }
        if (object.containsField("language")) {
            dto.setLanguage((String) object.get("language"));
        }
        if (object.containsField("securityToken")) {
            dto.setSecurityToken((String) object.get("securityToken"));
        }

        if (object.containsField("sender")) {
            dto.setSender((String) object.get("sender"));
        }
        return dto;
    }
}
