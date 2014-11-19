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
package at.ac.tuwien.dsg.pm.dao;

import at.ac.tuwien.dsg.pm.exceptions.CollectiveAlreadyExistsException;
import at.ac.tuwien.dsg.pm.model.Collective;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import com.mongodb.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MongoDBCollectiveDAO implements CollectiveDAO {

    private static final Logger log = LoggerFactory.getLogger(MongoDBPeerDAO.class);
    private static final String COLLECTION = "COLLECTIVES";

    private final DBCollection coll;

    public MongoDBCollectiveDAO(String host, int port, String database) throws UnknownHostException {
        this(new MongoClient(host, port), database, COLLECTION);
    }

    public MongoDBCollectiveDAO(MongoClient client, String database, String collection) {
        coll = client.getDB(database).getCollection(collection);
    }

    @Override
    public Collective addCollective(Collective collective) throws CollectiveAlreadyExistsException {
        DBObject object = serializeCollective(collective);
        if (collective.getId() == null) {
            object.put("_id", new ObjectId().toString());
        }
        try {
            //try to insert the new document
            coll.insert(object);
            if (collective.getId() == null) {
                collective.setId(String.valueOf(object.get("_id")));
            }

            log.trace("Created document for peer: {}", object);
        } catch (DuplicateKeyException e) {
            log.trace("Peer already exists: {}", object);
            throw new CollectiveAlreadyExistsException(e);
        }

        return collective;
    }

    @Override
    public Collective getCollective(String id) {
        return deserializeCollective(coll.findOne(new BasicDBObject("_id", id)));
    }

    @Override
    public List<Collective> getAll() {
        DBCursor dbObjects = coll.find();
        final List<Collective> collectives = new ArrayList<>(dbObjects.size());

        for (DBObject dbObject : dbObjects) {
            collectives.add(deserializeCollective(dbObject));
        }

        return collectives;
    }

    @Override
    public Collective updateCollective(Collective collective) {
        DBObject modify = coll.findAndModify(new BasicDBObject("_id", collective.getId()), null, null, false, new BasicDBObject("$set", new BasicDBObject("deliveryPolicy", collective.getDeliveryPolicy().name())), true, false);
        return deserializeCollective(modify);
    }

    @Override
    public Collective addPeerToCollective(String collectiveId, String peerId) {
        DBObject modify = coll.findAndModify(new BasicDBObject("_id", collectiveId), null, null, false, new BasicDBObject("$addToSet", new BasicDBObject("peers", peerId)), true, false);
        return deserializeCollective(modify);
    }

    @Override
    public Collective removePeerToCollective(String collectiveId, String peerId) {
        DBObject modify = coll.findAndModify(new BasicDBObject("_id", collectiveId), null, null, false, new BasicDBObject("$pull", new BasicDBObject("peers", peerId)), true, false);
        return deserializeCollective(modify);
    }

    @Override
    public Collective deleteCollective(String id) {
        return deserializeCollective(coll.findAndRemove(new BasicDBObject("_id", id)));
    }

    @Override
    public void clearData() {
        coll.remove(new BasicDBObject());
    }

    private DBObject serializeCollective(Collective collective) {
        return new BasicDBObject()
                .append("_id", collective.getId())
                .append("deliveryPolicy", collective.getDeliveryPolicy().name())
                .append("peers", collective.getPeers());
    }

    private Collective deserializeCollective(DBObject object) {
        if (object == null) {
            return null;
        }

        Collective collective = new Collective();
        collective.setId(String.valueOf(object.get("_id")));
        collective.setDeliveryPolicy(DeliveryPolicy.Collective.valueOf(String.valueOf(object.get("deliveryPolicy"))));
        collective.setPeers(new ArrayList<>((List<String>) object.get("peers")));

        return collective;
    }
}
