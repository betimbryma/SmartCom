package at.ac.tuwien.dsg.smartcom.manager.am.dao;

import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MongoDBResolverDAO implements ResolverDAO {
    private static final Logger log = LoggerFactory.getLogger(MongoDBResolverDAO.class);
    private static final String RESOLVER_COLLECTION = "PEER_ADDRESS_RESOLVER_COLLECTION";

    private final DBCollection coll;

    public MongoDBResolverDAO(String host, int port, String database) throws UnknownHostException {
        this(new MongoClient(host, port), database, RESOLVER_COLLECTION);
    }

    public MongoDBResolverDAO(MongoClient client, String database, String collection) {
        coll = client.getDB(database).getCollection(collection);
    }

    @Override
    public void insert(PeerAddress address) {
        BasicDBObject doc = serializePeerAddress(address);
        coll.insert(doc);
    }

    BasicDBObject serializePeerAddress(PeerAddress address) {
        BasicDBObject contactParams = new BasicDBObject();
        int i = 0;
        for (Serializable o : address.getContactParameters()) {
            contactParams.append((i++)+"", o);
        }

        BasicDBObject doc = new BasicDBObject()
                .append("_id", address.getPeerId()+"."+address.getAdapter())
                .append("peerId", address.getPeerId())
                .append("adapterId", address.getAdapter())
                .append("contactParameters", contactParams);
        log.debug("Saving peeraddress in mongoDB: ()", doc);
        return doc;
    }

    @Override
    public PeerAddress find(String peerId, String adapterId) {
        BasicDBObject query = new BasicDBObject("_id", peerId+"."+adapterId);

        PeerAddress address = null;
        DBObject one = coll.findOne(query);
        if (one != null) {
            address = deserializePeerAddress(one);
        }

        log.debug("Found peer address for query (): ()", query, address);
        return address;
    }

    @Override
    public void remove(String peerId, String adapterId) {
        coll.remove(new BasicDBObject("_id", peerId+"."+adapterId));
    }

    PeerAddress deserializePeerAddress(DBObject next) {
        PeerAddress address;List<Serializable> list = new ArrayList<>();
        DBObject contactParameters = (DBObject) next.get("contactParameters");
        int i = 0;
        while (contactParameters.containsField(i + "")) {
            list.add((Serializable) contactParameters.get((i++) + ""));
        }

        address = new PeerAddress((String) next.get("peerId"), (String) next.get("adapterId"), list);
        return address;
    }
}
