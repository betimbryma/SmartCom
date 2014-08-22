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
package at.ac.tuwien.dsg.smartcom.manager.auth.dao;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.Date;

/**
 * Provides access to a mongoDB to store authentication sessions in the database.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MongoDBAuthenticationSessionDAO implements AuthenticationSessionDAO {
    private static final Logger log = LoggerFactory.getLogger(MongoDBAuthenticationSessionDAO.class);
    private static final String SESSON_COLLECTION = "AUTHENTICATION_COLLECTION";

    private final DBCollection coll;

    /**
     * Create a new MongoDB Authentication Session DAO providing the host address, the port and the database that should
     * be used.
     *
     * @param host of the database
     * @param port of the database
     * @param database name of the database
     * @throws UnknownHostException could not determine the host
     */
    public MongoDBAuthenticationSessionDAO(String host, int port, String database) throws UnknownHostException {
        this(new MongoClient(host, port), database, SESSON_COLLECTION);
    }

    /**
     * Create a new MongoDB Authentication Session DAO providing a client, the database and the collection that should be used.
     * @param client mongoDB client that is already connected
     * @param database that should be used
     * @param collection that should be used
     */
    public MongoDBAuthenticationSessionDAO(MongoClient client, String database, String collection) {
        coll = client.getDB(database).getCollection(collection);
    }

    @Override
    public void persistSession(Identifier peerId, String token, Date expires) {
        log.trace("Persisting authentication session to log: Peer='{}', Token='{}', expires='{}'", peerId, token, expires);
        BasicDBObject dbObject = new BasicDBObject()
                .append("_id", peerId.getId())
                .append("token", token)
                .append("expires", expires);
        try {
            //try to insert the new document
            coll.insert(dbObject);
            log.trace("Created document for message: {}", dbObject);
        } catch (DuplicateKeyException e) {

            //there is already such a document, update it!
            coll.update(new BasicDBObject("_id", peerId.getId()), dbObject);
            log.trace("Updated document for message: {}", dbObject);
        }
    }

    @Override
    public boolean isValidSession(Identifier peerId, String token) {
        //check if there is a document with the given id and token
        //check if the session is still valid (expiration timestamp is in the future)
        BasicDBObject dbObject = new BasicDBObject()
                .append("_id", peerId.getId())
                .append("token", token)
                .append("expires", BasicDBObjectBuilder.start("$gte", new Date()).get());

        //if there is such a document, it would not be null
        return coll.findOne(dbObject) != null;
    }
}
