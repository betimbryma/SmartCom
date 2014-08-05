package at.ac.tuwien.dsg.smartcom.manager.auth.dao;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.Date;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MongoDBAuthenticationSessionDAO implements AuthenticationSessionDAO {
    private static final Logger log = LoggerFactory.getLogger(MongoDBAuthenticationSessionDAO.class);
    private static final String SESSON_COLLECTION = "AUTHENTICATION_COLLECTION";

    private final DBCollection coll;

    public MongoDBAuthenticationSessionDAO(String host, int port, String database) throws UnknownHostException {
        this(new MongoClient(host, port), database, SESSON_COLLECTION);
    }

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
        log.trace("Created document for message: {}", dbObject);
        try {
            coll.insert(dbObject);
        } catch (DuplicateKeyException e) {
            coll.update(new BasicDBObject("_id", peerId.getId()), dbObject);
        }
    }

    @Override
    public boolean isValidSession(Identifier peerId, String token) {
        BasicDBObject dbObject = new BasicDBObject()
                .append("_id", peerId.getId())
                .append("token", token)
                .append("expires", BasicDBObjectBuilder.start("$gte", new Date()).get());
        return coll.findOne(dbObject) != null;
    }
}
