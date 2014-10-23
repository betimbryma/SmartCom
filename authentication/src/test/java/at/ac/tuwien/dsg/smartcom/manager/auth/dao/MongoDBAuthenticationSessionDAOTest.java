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
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

public class MongoDBAuthenticationSessionDAOTest {

    protected MongoDBInstance mongoDB;
    private DBCollection collection;
    private MongoDBAuthenticationSessionDAO dao;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        MongoClient mongo = mongoDB.getClient();
        collection = mongo.getDB("test-session").getCollection("session");
        dao = new MongoDBAuthenticationSessionDAO(mongo, "test-session", "session");
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
    }

    @Test
    public void testInsertSession() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.AUGUST, 5, 13, 24, 30);

        dao.persistSession(Identifier.peer("test1"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test2"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test3"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test4"), "token1", calendar.getTime());

        assertEquals(4, collection.getCount());

        calendar.add(Calendar.HOUR, 4);
        dao.persistSession(Identifier.peer("test3"), "token2", calendar.getTime());

        assertEquals(4, collection.getCount());
        assertEquals(calendar.getTime(), collection.find(new BasicDBObject().append("_id", "test3")).next().get("expires"));
    }

    @Test
    public void testIsValidSession() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);

        dao.persistSession(Identifier.peer("test1"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test2"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test3"), "token1", calendar.getTime());
        dao.persistSession(Identifier.peer("test4"), "token1", calendar.getTime());

        assertTrue("Session seems not to be valid but should be!", dao.isValidSession(Identifier.peer("test2"), "token1"));
        assertFalse("Session seems to be valid but shouldn't!", dao.isValidSession(Identifier.peer("test1"), "token3"));

        dao.persistSession(Identifier.peer("test4"), "token3", calendar.getTime());
        assertTrue("Session seems not to be valid but should be!", dao.isValidSession(Identifier.peer("test4"), "token3"));

        calendar.add(Calendar.HOUR, -1);
        dao.persistSession(Identifier.peer("test3"), "token1", calendar.getTime());
        assertFalse("Session seems to be valid but shouldn't!", dao.isValidSession(Identifier.peer("test3"), "token1"));
    }
}