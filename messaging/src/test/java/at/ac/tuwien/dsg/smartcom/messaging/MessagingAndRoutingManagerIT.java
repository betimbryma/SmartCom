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
package at.ac.tuwien.dsg.smartcom.messaging;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.callback.CollectiveInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.MessagingAndRoutingManager;
import at.ac.tuwien.dsg.smartcom.manager.am.AdapterExecutionEngine;
import at.ac.tuwien.dsg.smartcom.manager.am.AdapterManagerImpl;
import at.ac.tuwien.dsg.smartcom.manager.am.AddressResolver;
import at.ac.tuwien.dsg.smartcom.manager.dao.MongoDBPeerChannelAddressResolverDAO;
import at.ac.tuwien.dsg.smartcom.messaging.policies.privacy.peer.AlwaysFailsDummyPeerPrivacyPolicy;
import at.ac.tuwien.dsg.smartcom.model.*;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import at.ac.tuwien.dsg.smartcom.utils.*;

import com.mongodb.MongoClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;

public class MessagingAndRoutingManagerIT {

    private PicoHelper pico;
    private PeerInfoService peerInfoService;
    private AdapterManager adManager;
    private MessagingAndRoutingManager mrMgr;
    final Lock lock = new ReentrantLock();
    final Condition receivedMessage = lock.newCondition();
    
    private MongoClient mongo;
    private MongoDBInstance mongoDB;
    


    @Before
    public void setUp() throws Exception {
    	
    	mongoDB = new MongoDBInstance();
        mongoDB.setUp();
        mongo = new MongoClient("localhost", 12345);
    	
        pico = new PicoHelper();
        pico.addComponent(MessagingAndRoutingManagerImpl.class);
        pico.addComponent(new PeerInfoServiceImpl_TestLocal());
        pico.addComponent(new CollectiveInfoCallback_TestLocal());
        pico.addComponent(SimpleMessageBroker.class);
        
        
        
        
        pico.addComponent(AdapterManagerImpl.class);
        pico.addComponent(AdapterExecutionEngine.class);
        pico.addComponent(AddressResolver.class);
        pico.addComponent(new MongoDBPeerChannelAddressResolverDAO(mongo, "test-resolver", "resolver"));
        pico.addComponent(StatisticBean.class);
        
    	peerInfoService = pico.getComponent(PeerInfoService.class);
    	mrMgr = pico.getComponent(MessagingAndRoutingManager.class);
    	
    	adManager = pico.getComponent(AdapterManager.class);
    	
    	//registers the new output adapter type. Since it is a stateful one, and a new instance will get instantiated when needed, we do not need the return value. It would make sense in case of a stateful one.
    	adManager.registerOutputAdapter(at.ac.tuwien.dsg.smartcom.messaging.adapter.StatefulAdapter.class);  	

        pico.start();
    }

    @After
    public void tearDown() throws Exception {
    	
    	mongoDB.tearDown();
        mongo.close();
    	
        pico.stop();
    }

    
    @Test
    public void testCollectiveDeliveryAcknowledged() throws Exception {
    	
    	
    	for (int i=1; i<4; i++){
    		//creates an instance of stateful adapter, and returns its Identifier
    		PeerInfo pinf = peerInfoService.getPeerInfo(Identifier.peer("peer" + i));
    		assertNotNull(pinf);
    	}
    	
    	
    	Message msg = new Message.MessageBuilder()
    							 .setContent("msg 1 contents)")
    							 .setReceiverId(Identifier.collective("col1"))
    							 .setSenderId(PredefinedMessageHelper.taskExecutionEngine)
    							 .setConversationId("conversation 1")
    							 .setWantsAcknowledgement(true)
    							 .setType(null) 
    							 .create();
    	
    	NotificationCallback_TestLocal receiver = new NotificationCallback_TestLocal(lock, receivedMessage);
    	Identifier receiverId = mrMgr.registerNotificationCallback(receiver);
    	
    	
    	
    	lock.lock();
        try {
        	mrMgr.send(msg);
        	receivedMessage.await();
        	Message receivedMessage = receiver.receivedMessage;
        	
        	assertNotNull("Received message should not be null", receivedMessage);
        	assertEquals("Received message should be an ACK", PredefinedMessageHelper.ACK_SUBTYPE_CHECKED, receivedMessage.getSubtype());
        	assertEquals("RefersTo() field of the ACK message should correspond to the ID of the sent message", msg.getId().getId(), receivedMessage.getRefersTo().getId());
        	
        }finally {
            lock.unlock();
            mrMgr.unregisterNotificationCallback(receiverId);
        }
	
     
    }
    

    @Test
    public void testCollectiveDeliveryUnacknowledged() throws Exception {
    	
    	
    	Message msg = new Message.MessageBuilder()
    							 .setContent("msg 1 contents)")
    							 .setReceiverId(Identifier.collective("col1"))
    							 .setSenderId(PredefinedMessageHelper.taskExecutionEngine)
    							 .setConversationId("conversation 1")
    							 .setWantsAcknowledgement(false)
    							 .setType(null) 
    							 .create();
    	
    	NotificationCallback_TestLocal receiver = new NotificationCallback_TestLocal(lock, receivedMessage);
    	Identifier receiverId = mrMgr.registerNotificationCallback(receiver);
    	
    	
    	
    	lock.lock();
        try {
        	mrMgr.send(msg);
        	receivedMessage.await(6000, TimeUnit.MILLISECONDS);
        	Message receivedMessage = receiver.receivedMessage;
        	
        	assertNull("Received message should be null", receivedMessage);

        	
        }finally {
            lock.unlock();
            mrMgr.unregisterNotificationCallback(receiverId);
        }
	
     
    }
    
    
    
    
    
    
    
    
    
    
    
    private class CollectiveInfoCallback_TestLocal implements CollectiveInfoCallback {
    	

		@Override
		public CollectiveInfo getCollectiveInfo(Identifier collective)
				throws NoSuchCollectiveException {
			
			CollectiveInfo newCol = new CollectiveInfo();
			
			Identifier peer1 = new Identifier(IdentifierType.PEER, "peer1", "");
			Identifier peer2 = new Identifier(IdentifierType.PEER, "peer2", "");
			Identifier peer3 = new Identifier(IdentifierType.PEER, "peer3", "");
			
			ArrayList<Identifier> peers = new ArrayList<Identifier>(3);
			
			peers.add(peer1);
			peers.add(peer2);
			peers.add(peer3);
			
			newCol.setPeers(peers);
			newCol.setDeliveryPolicy(at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy.Collective.TO_ANY);
			newCol.setId(Identifier.collective("col1"));
			return newCol;
		}
    	
    }
    
    private class NotificationCallback_TestLocal implements NotificationCallback{
    	
    	private Lock l;
    	private Condition c;
    	Message receivedMessage = null;
    	
    	public NotificationCallback_TestLocal(Lock l, Condition c){
    		this.l = l;
    		this.c = c;
    	}
    	
		@Override
		public void notify(Message message) {
			l.lock();
			try {
					receivedMessage = message;
					c.signal();
			} finally {
			       l.unlock();
			}
			
		}
    	
    	
    }
    
    private class PeerInfoServiceImpl_TestLocal implements PeerInfoService {

        private Map<Identifier, PeerInfo> peerInfoMap = new HashMap<>();
        private AtomicInteger retrieveCounter = new AtomicInteger(0);

        @SuppressWarnings("unchecked")
		private PeerInfoServiceImpl_TestLocal() {
        	Identifier peerId1 = Identifier.peer("peer1");
        	List<PeerChannelAddress> addresses1 = new ArrayList<PeerChannelAddress>();
        	addresses1.add(new PeerChannelAddress(peerId1, Identifier.channelType("stateful"), Collections.EMPTY_LIST));
        	//addresses1.add(new PeerChannelAddress(peerId1, Identifier.channelType("stateful"), Collections.EMPTY_LIST));
        	//addresses1.add(new PeerChannelAddress(peerId1, Identifier.channelType("stateful"), Collections.EMPTY_LIST));
        	peerInfoMap.put(peerId1, new PeerInfo(peerId1, DeliveryPolicy.Peer.PREFERRED, Collections.<PrivacyPolicy> emptyList(), addresses1));
        	
        	Identifier peerId2 = Identifier.peer("peer2");
        	List<PeerChannelAddress> addresses2 = new ArrayList<PeerChannelAddress>();
        	List<PrivacyPolicy> privPolicies = new ArrayList<PrivacyPolicy>();
        	privPolicies.add(new AlwaysFailsDummyPeerPrivacyPolicy());
        	addresses2.add(new PeerChannelAddress(peerId2, Identifier.channelType("stateful"), Collections.EMPTY_LIST));
        	peerInfoMap.put(peerId2, new PeerInfo(peerId2, DeliveryPolicy.Peer.TO_ALL_CHANNELS, privPolicies, addresses2));
        	
        	Identifier peerId3 = Identifier.peer("peer3");
        	List<PeerChannelAddress> addresses3 = new ArrayList<PeerChannelAddress>();
        	//addresses3.add(new PeerChannelAddress(peerId3, Identifier.channelType("channelType1"), Collections.EMPTY_LIST));
        	addresses3.add(new PeerChannelAddress(peerId3, Identifier.channelType("stateful"), Collections.EMPTY_LIST));
        	peerInfoMap.put(peerId3, new PeerInfo(peerId3, DeliveryPolicy.Peer.TO_ALL_CHANNELS, Collections.<PrivacyPolicy> emptyList(), addresses3));
            
        }

        @Override
        public PeerInfo getPeerInfo(Identifier id) throws NoSuchPeerException {
            retrieveCounter.incrementAndGet();
            return peerInfoMap.get(id);
        }

        public int getRetrieveCounter() {
            return retrieveCounter.intValue();
        }
    }


    
}