package at.ac.tuwien.dsg.smartcom.manager.am.dao;

import at.ac.tuwien.dsg.smartcom.manager.am.utils.MongoDBInstance;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class ResolverMongoDBDAOTest {

    private final Identifier peer1 = Identifier.peer("peer1");
    private final Identifier peer2 = Identifier.peer("peer2");
    private final Identifier adapter1 = Identifier.adapter("adapter1");
    private final Identifier adapter2 = Identifier.adapter("adapter2");
    private MongoDBInstance mongoDB;

    private MongoDBResolverDAO resolver;
    private DBCollection collection;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        MongoClient mongo = new MongoClient("localhost", 12345);
        collection = mongo.getDB("test-resolver").getCollection("resolver");
        resolver = new MongoDBResolverDAO(mongo, "test-resolver", "resolver");
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
    }

    @Test
    public void testInsert() throws Exception {
        PeerAddress address1 = new PeerAddress(peer1, adapter1, Collections.EMPTY_LIST);
        PeerAddress address2 = new PeerAddress(this.peer1, adapter2, Collections.EMPTY_LIST);
        PeerAddress address3 = new PeerAddress(peer2, adapter1, Collections.EMPTY_LIST);

        resolver.insert(address1);
        resolver.insert(address2);
        resolver.insert(address3);

        assertEquals("Not enough addresses saved!", 3, collection.count());

        boolean address1found = false;
        boolean address2found = false;
        boolean address3found = false;

        for (DBObject dbObject : collection.find()) {
            PeerAddress address = resolver.deserializePeerAddress(dbObject);
            assertNotNull("PeerId null!", address.getPeerId());
            assertNotNull("AdapterId null!", address.getAdapterId());
            assertNotNull("Parameters null!", address.getContactParameters());

            if ("peer1".equals(address.getPeerId().getId())) {
                if ("adapter1".equals(address.getAdapterId().getId()) && address.equals(address1)) {
                    address1found = true;
                } else if ("adapter2".equals(address.getAdapterId().getId()) && address.equals(address2)) {
                    address2found = true;
                } else {
                    fail("address does not match any inserted addresses!");
                }
            } else if ("peer2".equals(address.getPeerId().getId()) && "adapter1".equals(address.getAdapterId().getId()) && address.equals(address3)) {
                address3found = true;
            } else {
                fail("address does not match any inserted addresses!");
            }
        }

        assertTrue("Address1 not found!", address1found);
        assertTrue("Address2 not found!", address3found);
        assertTrue("Address3 not found!", address2found);
    }

    @Test
    public void testFind() throws Exception {
        PeerAddress address1 = new PeerAddress(peer1, adapter1, Collections.EMPTY_LIST);
        PeerAddress address2 = new PeerAddress(peer1, adapter2, Collections.EMPTY_LIST);
        PeerAddress address3 = new PeerAddress(peer2, adapter1, Collections.EMPTY_LIST);

        collection.insert(resolver.serializePeerAddress(address1));
        collection.insert(resolver.serializePeerAddress(address2));
        collection.insert(resolver.serializePeerAddress(address3));

        for (DBObject dbObject : collection.find()) {
            System.out.println(dbObject);
        }


        assertEquals("Retrieved peerAddress does not match the required!", address1, resolver.find(peer1, adapter1));
        assertEquals("Retrieved peerAddress does not match the required!", address2, resolver.find(peer1, adapter2));
        assertEquals("Retrieved peerAddress does not match the required!", address3, resolver.find(peer2, adapter1));

        assertNull("Retrieved peerAddress is not null but should not exist!", resolver.find(Identifier.peer("peer3"), Identifier.adapter("adapter1")));
    }

    @Test
    public void testRemove() throws Exception {
        PeerAddress address1 = new PeerAddress(peer1, adapter1, Collections.EMPTY_LIST);
        PeerAddress address2 = new PeerAddress(peer1, adapter2, Collections.EMPTY_LIST);
        PeerAddress address3 = new PeerAddress(peer2, adapter1, Collections.EMPTY_LIST);

        collection.insert(resolver.serializePeerAddress(address1));
        collection.insert(resolver.serializePeerAddress(address2));
        collection.insert(resolver.serializePeerAddress(address3));

        for (DBObject dbObject : collection.find()) {
            System.out.println(dbObject);
        }

        long count = collection.count();

        resolver.remove(peer1, adapter1);

        assertEquals("There is still the same amount of addresses in the DB!", count-1, collection.count());

        assertEquals("Retrieved peerAddress does not match the required!", address2, resolver.find(peer1, adapter2));
        assertEquals("Retrieved peerAddress does not match the required!", address3, resolver.find(peer2, adapter1));

        assertNull("Retrieved peerAddress is not null but should not exist!", resolver.find(peer1, adapter1));
    }
}