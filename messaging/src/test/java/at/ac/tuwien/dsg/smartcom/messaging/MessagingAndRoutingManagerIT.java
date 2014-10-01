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
import at.ac.tuwien.dsg.smartcom.utils.MongoDBInstance;
import at.ac.tuwien.dsg.smartcom.utils.PicoHelper;
import at.ac.tuwien.dsg.smartcom.utils.PredefinedMessageHelper;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertNotNull;

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
    	  	

        pico.start();
    }

    @After
    public void tearDown() throws Exception {
    	
    	mongoDB.tearDown();
        mongo.close();
    	
        pico.stop();
    }

    //TODO This test does not work at all... I (philipp) tried to fix it but the whole test setup seems to be wrong, please fix it.
//    @Test
    public void test() throws Exception {
    	
    	//registers the new output adapter type. Since it is a stateful one, and a new instance will get instantiated when needed, we do not need the return value. It would make sense in case of a stateful one.
    	adManager.registerOutputAdapter(at.ac.tuwien.dsg.smartcom.messaging.adapter.StatefulAdapter.class); 
    	
    	
    	Identifier[] outputAdapters = new Identifier[3];
    	Identifier[] inputAdapters = new Identifier[3];
    	
    	for (int i=1; i<4; i++){
    		//creates an instance of stateful adapter, and returns its Identifier
    		PeerInfo pinf = peerInfoService.getPeerInfo(Identifier.peer("peer" + i));
    		assertNotNull(pinf);
    		//outputAdapters[i-1] = adManager.createEndpointForPeer(pinf).get(0);
    		//inputAdapters[i-1] = adManager.addPullAdapter(new TestInputPullAdapter("peer"+ i + ".stateful"), 2000, false);
    	}
    	
    	
    	Message msg = new Message.MessageBuilder()
    							 .setContent("msg 1 contents)")
    							 .setReceiverId(Identifier.collective("col1"))
    							 .setSenderId(PredefinedMessageHelper.taskExecutionEngine)
    							 .setConversationId("conversation 1")
    							 .setType(null) //TODO What to set here?
    							 .create();
    	
    	NotificationCallback_TestLocal receiver = new NotificationCallback_TestLocal(lock, receivedMessage);
    	mrMgr.registerNotificationCallback(receiver);
    	
    	lock.lock();
        try {
        	mrMgr.send(msg);
        	receivedMessage.await();
        	Message receivedMessage = receiver.receivedMessage;
        	
        	assertNotNull(receivedMessage);
        	//assertEquals("ACK", receivedMessage.getSubtype());
        	
        }finally {
            lock.unlock();
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