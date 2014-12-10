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

import at.ac.tuwien.dsg.smartcom.model.MessageInformation;
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
public class MongoDBMessageInfoDAO implements MessageInfoDAO {
    private static final Logger log = LoggerFactory.getLogger(MongoDBMessageInfoDAO.class);
    private static final String COLLECTION = "MESSAGE_INFO_COLLECTION";

    private final DBCollection coll;

    /**
     * Create a new MongoDB message info DAO providing the host and port of the MongoDB instance and
     * the name of the database that should be used. It will use the default collection of this
     * resolver in the MongoDB database.
     *
     * @param host address of the MongoDB instance
     * @param port port number of the MongoDB instance
     * @param database name of the database that should be used.
     * @throws java.net.UnknownHostException if the database cannot be resolved
     * @see MongoDBMessageInfoDAO#COLLECTION
     */
    public MongoDBMessageInfoDAO(String host, int port, String database) throws UnknownHostException {
        this(new MongoClient(host, port), database, COLLECTION);
    }

    /**
     * Create a new MongoDB message info DAO providing an already created client for a MongoDB instance,
     * the name of the database and a collection that should be used to save, retrieve and delete
     * entries.
     *
     * This constructor is especially useful for unit testing because data can be preloaded in the
     * specified collection or the presence of added entries can be checked.
     *
     * @param client MongoClient that is connected to a database
     * @param database name of the database that should be used
     * @param collection name of the collection that should be used
     */
    public MongoDBMessageInfoDAO(MongoClient client, String database, String collection) {
        coll = client.getDB(database).getCollection(collection);
    }

    @Override
    public void insert(MessageInformation information) {
        BasicDBObject doc = serializeMessageInfo(information);
        coll.update(
                new BasicDBObject("_id", serializeKey(information.getKey())),
                doc, true, false);
        log.trace("Inserted/Updated document for MessageInformation: {}", doc);
    }

    @Override
    public MessageInformation find(MessageInformation.Key key) {
        BasicDBObject query = new BasicDBObject("_id", serializeKey(key));

        MessageInformation info = null;
        DBObject one = coll.findOne(query);
        if (one != null) {
            info = deserializeMessageInfo(one);
        }

        log.trace("Found message info for query {}: {}", query, info);
        return info;
    }

    BasicDBObject serializeMessageInfo(MessageInformation mi) {
        BasicDBObject doc = new BasicDBObject();
        doc.append("_id", serializeKey(mi.getKey()));
        doc.append("purpose", mi.getPurpose());
        doc.append("validAnswer", mi.getValidAnswer());

        BasicDBList types = new BasicDBList();
        for (MessageInformation.Key key : mi.getValidAnswerTypes()) {
            types.add(serializeKey(key));
        }
        doc.append("validAnswerTypes", types);

        BasicDBList rel = new BasicDBList();
        for (MessageInformation.Key key : mi.getRelatedMessages()) {
            rel.add(serializeKey(key));
        }
        doc.append("relatedMessages", rel);

        log.trace("Saving message info in mongoDB: {}", doc);
        return doc;
    }

    BasicDBObject serializeKey(MessageInformation.Key key) {
        return new BasicDBObject("type", key.getType()).append("subtype", key.getSubtype());
    }

    MessageInformation deserializeMessageInfo(DBObject dbObject) {
        MessageInformation info = new MessageInformation();

        if (dbObject.containsField("_id")) {
            info.setKey(deserializeKey((DBObject) dbObject.get("_id")));
        }

        if (dbObject.containsField("purpose")) {
            info.setPurpose((String) dbObject.get("purpose"));
        }

        if (dbObject.containsField("validAnswer")) {
            info.setValidAnswer((String) dbObject.get("validAnswer"));
        }

        if (dbObject.containsField("validAnswerTypes")) {
            List<MessageInformation.Key> validAnswers = new ArrayList<>();

            BasicDBList list = (BasicDBList) dbObject.get("validAnswerTypes");
            for (Object o : list) {
                validAnswers.add(deserializeKey((DBObject) o));
            }
            info.setValidAnswerTypes(validAnswers);
        }

        if (dbObject.containsField("relatedMessages")) {
            List<MessageInformation.Key> related = new ArrayList<>();

            BasicDBList list = (BasicDBList) dbObject.get("relatedMessages");
            for (Object o : list) {
                related.add(deserializeKey((DBObject) o));
            }
            info.setRelatedMessages(related);
        }

        return info;
    }

    MessageInformation.Key deserializeKey(DBObject dbObject) {
        String type = (String) dbObject.get("type");
        String subtype = (String) dbObject.get("subtype");

        return new MessageInformation.Key(type, subtype);
    }
}
