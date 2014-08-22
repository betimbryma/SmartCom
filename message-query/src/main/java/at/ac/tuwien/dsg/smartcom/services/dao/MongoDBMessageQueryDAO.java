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
package at.ac.tuwien.dsg.smartcom.services.dao;

import at.ac.tuwien.dsg.smartcom.exception.IllegalQueryException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.IdentifierType;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.services.QueryCriteriaImpl;
import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MongoDBMessageQueryDAO implements MessageQueryDAO {
    private static final String LOGGING_COLLECTION = "MESSAGE_LOGGING_COLLECTION";

    private final DBCollection coll;

    public MongoDBMessageQueryDAO(String host, int port, String database) throws UnknownHostException {
        this(new MongoClient(host, port), database, LOGGING_COLLECTION);
    }

    public MongoDBMessageQueryDAO(MongoClient client, String database, String collection) {
        coll = client.getDB(database).getCollection(collection);
    }

    @Override
    public Collection<Message> query(QueryCriteriaImpl criteria) throws IllegalQueryException {
        BasicDBObject template = createTemplate(criteria.getBuilder().create(), criteria.getFrom(), criteria.getTo());

        DBCursor dbObjects = coll.find(template);
        Collection<Message> messages = new ArrayList<>();
        for (DBObject dbObject : dbObjects) {
            messages.add(deserialize(dbObject));
        }
        return messages;
    }

    private BasicDBObject createTemplate(Message message, Date from, Date to) {
        BasicDBObject query = new BasicDBObject();

        if (message.getId() != null) {
            query.put("_id", message.getId().getId());
        }

        if (message.getType() != null) {
            query.put("type", message.getType());
        }

        if (message.getSubtype() != null) {
            query.put("subtype", message.getSubtype());
        }

        if (message.getSenderId() != null) {
            query.put("sender", serializeIdentifier(message.getSenderId()));
        }

        if (message.getReceiverId() != null) {
            query.put("receiver", serializeIdentifier(message.getReceiverId()));
        }

        if (message.getConversationId() != null) {
            query.put("conversationId", message.getConversationId());
        }

        if (from != null && to != null) {
            query.put("created", BasicDBObjectBuilder.start("$gte", from).add("$lte", to).get());
        } else if (from != null) {
            query.put("created", BasicDBObjectBuilder.start("$gte", from).get());
        } else if (to != null) {
            query.put("created", BasicDBObjectBuilder.start("$lte", to).get());
        }

        return query;
    }

    private BasicDBObject serializeIdentifier(Identifier identifier) {
        return new BasicDBObject()
                .append("type", identifier.getType().toString())
                .append("id", identifier.getId());
    }

    private Message deserialize(DBObject object) {
        Message.MessageBuilder messageBuilder = new Message.MessageBuilder()
                .setId(Identifier.message((String) object.get("_id")))
                .setType((String) object.get("type"))
                .setSubtype((String) object.get("subtype"))
                .setSenderId(deserializeIdentifier((DBObject) object.get("sender")))
                .setReceiverId(deserializeIdentifier((DBObject) object.get("receiver")))
                .setContent((String) object.get("content"))
                .setConversationId((String) object.get("conversationId"))
                .setTtl((Long) object.get("ttl"))
                .setLanguage((String) object.get("language"))
                .setSecurityToken((String) object.get("securityToken"));
        return messageBuilder.create();
    }

    private Identifier deserializeIdentifier(DBObject id) {
        IdentifierType type = IdentifierType.valueOf((String) id.get("type"));
        String identifier = (String) id.get("id");
        return new Identifier(type, identifier, "");
    }
}
