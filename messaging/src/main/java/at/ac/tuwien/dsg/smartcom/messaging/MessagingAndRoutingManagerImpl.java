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

import at.ac.tuwien.dsg.smartcom.broker.*;
import at.ac.tuwien.dsg.smartcom.broker.policy.DynamicReplicationPolicy;
import at.ac.tuwien.dsg.smartcom.callback.CollectiveInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.exception.ErrorCode;
import at.ac.tuwien.dsg.smartcom.exception.RoutingException;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.MessagingAndRoutingManager;
import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.collective.SimpleToAllCollectivePolicy;
import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.collective.SimpleToAnyCollectivePolicy;
import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.peer.SimpleAtLeastOnePeerPolicy;
import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.peer.SimplePreferredPeerPolicy;
import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.peer.SimpleToAllChannelsPeerPolicy;
import at.ac.tuwien.dsg.smartcom.model.*;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import at.ac.tuwien.dsg.smartcom.utils.Pair;
import at.ac.tuwien.dsg.smartcom.utils.PredefinedMessageHelper;
import at.ac.tuwien.dsg.smartcom.utils.TimeBasedUUID;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;


/**
 * Default implementation of the Messaging & Routing Manager.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @author Ognjen Scekic
 * @version 1.0
 */
public class MessagingAndRoutingManagerImpl implements MessagingAndRoutingManager, MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MessagingAndRoutingManager.class);
	
	private final int COL_DS_SIZE = 1000;
	private final int PEER_DS_SIZE = 10 * COL_DS_SIZE; 
	private final int MAP_SIZE_LIMIT = 20000;

	private ExecutorService executor;
    private ExecutorService brokerExecutor;
    private ExecutorService logExecutor;

	private final ConcurrentHashMap<DeliveryPolicyDataStructureKey, DeliveryPolicy> collectiveDatastruct = new ConcurrentHashMap<DeliveryPolicyDataStructureKey, DeliveryPolicy>(COL_DS_SIZE);
	private final ConcurrentHashMap<DeliveryPolicyDataStructureKey, ArrayList<PeerPolicyDataStructureValueElement>> peerDatastruct = new ConcurrentHashMap<DeliveryPolicyDataStructureKey, ArrayList<PeerPolicyDataStructureValueElement>>(PEER_DS_SIZE);
	private final HashMap<Identifier, NotificationCallback> callbacks = new HashMap<Identifier, NotificationCallback>(); 
	private final HashMap<Identifier, RoutingRule> routingTable = new HashMap<Identifier, RoutingRule>();
	private final ConcurrentHashMap<PeerInfo, Pair<List<Identifier>, PeerInfo>>  peerToAdaptersMappings = new ConcurrentHashMap<PeerInfo, Pair<List<Identifier>, PeerInfo>>();

	@Inject
	private PeerInfoService peerInfoProvider; //inject instance of PeerInfoServiceImpl, which is our cache with the same API as the callback API for getting the PeerInfo from the PeerManager
	
	@Inject
	private CollectiveInfoCallback collectiveInfoProvider; //no implementations so far. Inject a dummy instance. This is not a local service reading our cache, but provided by PeerManager.
		
	@Inject
    private MessageBroker broker; 
	
	@Inject
	private AdapterManager adapterMgr;

    @Inject
    private StatisticBean statistic;

    private ReplicatingMessageListener inputListener;
    private ReplicatingMessageListener controlListener;
    private CancelableListener cancelableInputListener;
    private CancelableListener cancelableControlListener;

    /**
    * Initializes the executors for message sending.
    */
	@PostConstruct
    public void init() {
    	//registerNotificationCallback(null); //to register taskexecution engine as default
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("MARM-thread-%d").build();
    	executor = Executors.newFixedThreadPool(10, namedThreadFactory);

        ThreadFactory brokerThreadFactory = new ThreadFactoryBuilder().setNameFormat("Broker-thread-%d").build();
        brokerExecutor = Executors.newFixedThreadPool(10, brokerThreadFactory);

        ThreadFactory logThreadFactory = new ThreadFactoryBuilder().setNameFormat("LOG-thread-%d").setPriority(Thread.MIN_PRIORITY).build();
        logExecutor = Executors.newFixedThreadPool(10, logThreadFactory);

        inputListener = new ReplicatingMessageListener("input", this, new ReplicationFactory() {
            @Override
            public MessageListener createReplication() {
                return MessagingAndRoutingManagerImpl.this;
            }
        }, new DynamicReplicationPolicy());
        cancelableInputListener = broker.registerInputListener(inputListener);

        controlListener = new ReplicatingMessageListener("control", this, new ReplicationFactory() {
            @Override
            public MessageListener createReplication() {
                return MessagingAndRoutingManagerImpl.this;
            }
        }, new DynamicReplicationPolicy());
        cancelableControlListener = broker.registerControlListener(controlListener);
    }

    /**
     * Stops the executors for and cancels all active Futures.
     */
	@PreDestroy
    public void destroy() {
        log.info("Executor will be shut down");

        cancelableInputListener.cancel();
        cancelableControlListener.cancel();
        inputListener.shutdown();
        controlListener.shutdown();

        brokerExecutor.shutdown();
        try {
            if (!brokerExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                brokerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Could not await termination of executor. forcing shutdown", e);
            brokerExecutor.shutdownNow();
        }

        logExecutor.shutdown();
        try {
            if (!logExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                logExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Could not await termination of executor. forcing shutdown", e);
            logExecutor.shutdownNow();
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Could not await termination of executor. forcing shutdown", e);
            executor.shutdownNow();
        }

        log.info("Executor shutdown complete!");
    }

	@Override
	public Identifier send(final Message message) {
        statistic.sendingRequestReceived();

		if (message.getId() == null){
			message.setId(new Identifier(IdentifierType.MESSAGE, generateUniqueIdString(), ""));
		}else{
			log.warn("Message with a pre-set ID {} received. Discarding.", message.getId().getId());
			return null;
		}

        log.trace("Received message: {}", message);
        final MessageLogLevel logLevel = MessageLogLevel.NONE;
        //TODO add the configuration of the message log level either to the communication interface or as a constructor parameter of this class
        if (logLevel.equals(MessageLogLevel.NONE)) {
            logExecutor.submit(new Runnable() {

                @Override
                public void run() {
                    switch (logLevel) {
                        case ALL:
                            log();
                            break;
                        case INGOING:
                            //TODO
                            break;
                        case OUTGOING:
                            //TODO
                            break;
                        case EXTERNAL:
                            if (!PredefinedMessageHelper.CONTROL_TYPE.equals(message.getType())) {
                                log();
                            }
                            break;
                        case INTERNAL:
                            if (PredefinedMessageHelper.CONTROL_TYPE.equals(message.getType())) {
                                log();
                            }
                            break;
                        case NONE:
                            return; //should never reach this line but just in case
                    }
                }

                private void log() {
                    statistic.logRequest();
                    broker.publishLog(message.clone());
                }
            });
        }

		final MessagingAndRoutingManagerImpl router = this;
		
        executor.submit(
            new Runnable() {

                @Override
                public void run() {
                    if (PredefinedMessageHelper.CONTROL_TYPE.equals(message.getType()) &&
                            (PredefinedMessageHelper.ACK_SUBTYPE.equals(message.getSubtype()) ||
                            PredefinedMessageHelper.COMERROR_SUBTYPE.equals(message.getSubtype()))) {
                        statistic.internalMessageSendingRequest();
                        enforcePeerDeliveryPolicy(message);
                    } else {
                        statistic.externalMessageSendingRequest();
                        router.handleMessage(message);
                    }
                }
					
        });
		//futureList.add(future); //we probably won't need this at all, as sending should mean ultimately just 
		//passing the msg to the appropriate broker. 
		//Therefore, it would be just wasting time to track futures just to be able to cancel them.
	
		return message.getId();
	}

	@Override
	public Identifier addRouting(RoutingRule rule) {
		// TODO a temporary solution
		
		Identifier i = Identifier.routing(TimeBasedUUID.getUUIDAsString());
		routingTable.put(i, rule);
		return i;
	}

	@Override
	public RoutingRule removeRouting(Identifier routeId) {
		//TODO a temporary solution. 
		if (routeId == null) return null;
		RoutingRule r = routingTable.get(routeId);
		if (r != null) routingTable.remove(routeId);
		return r;
	}

	@Override
	public Identifier registerNotificationCallback(NotificationCallback callback) {
		
/*		if (callbacks.isEmpty()){
			callbacks.put(PredefinedMessageHelper.taskExecutionEngine, callback);
		}
*/
		
		if (callback != null && !callbacks.containsValue(callback)){
			Identifier i = Identifier.component(TimeBasedUUID.getUUIDAsString());
			callbacks.put(i, callback); //as a temporary solution
			return i;
		}
		
		return null;
	}


	
	@Override
	public boolean unregisterNotificationCallback(Identifier callback) {
		if (callback != null && callbacks.containsKey(callback)){
			callbacks.remove(callback);
			return true;
		}
		return false;
	}
	
	private void notifyCallbacks(Message msg) {
		for (NotificationCallback callback : callbacks.values()) {
            log.debug("Notifying TEE of the msg" + msg.toString());
            boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
			//if (!isDebug) 
            callback.notify(msg);
        }
	}
	
	private void handleMessage(Message msg)  {
	
		boolean isPrimaryRecipient = false; 
		ArrayList<Identifier> receiverList = new ArrayList<Identifier>();
		Identifier receiver = msg.getReceiverId();
		
		if (msg.getReceiverId() != null){
			receiverList.add(receiver); //this is the original receiver
			isPrimaryRecipient = true;	//ex createDataStruct. For the original receiver we need to create the data structure to track delivery of messages
		}
		
		receiverList.addAll(determineReceivers(msg));
		
/*		if (receiverList.isEmpty()) {
            for (NotificationCallback callback : callbacks.values()) {
                statistic.callbackCalled();
                callback.notify(msg);
            }
		}*/

        

		if (receiverList.isEmpty()){// || PredefinedMessageHelper.ACK_SUBTYPE_CHECKED.equals(msg.getSubtype())){
			notifyCallbacks(msg);
			return;
		}
		
		for (Identifier rec : receiverList){
			if (rec.getType() == IdentifierType.COMPONENT){
                statistic.componentMessageSendingRequest();
				handleComponentMessage(msg, rec);
			}else if (rec.getType() == IdentifierType.COLLECTIVE){
				try {
                    statistic.collectiveMessageSendingRequest();
					deliverToCollective(msg, rec, isPrimaryRecipient);
				} catch (NoSuchCollectiveException e) {
					Message errorMsg = PredefinedMessageHelper.createDeliveryErrorMessage(msg, "Attempted delivery to unknown or non-existent collective " + rec.getId() + ".", getMyIdentifier());
					send(errorMsg); 
					if (isPrimaryRecipient) return; //delivery to the original recipient failed. No need to loop over secondary recipients as well.
				}
			}else if (rec.getType() == IdentifierType.PEER){
				try {
                    statistic.peerMessageSendingRequest();
					deliverToPeer(msg, rec, isPrimaryRecipient, false);
				} catch (CommunicationException e) {
					Message errorMsg = PredefinedMessageHelper.createCommunicationErrorMessage(msg, "Delivery to peer " + rec.getId() + " failed.");
					enforcePeerDeliveryPolicy(errorMsg);
				} catch (Exception e){ //in case the peer was not found or adapter could not be instantiated
					Message errorMsg = PredefinedMessageHelper.createDeliveryErrorMessage(msg, "Delivery to peer " + rec.getId() + " failed.", getMyIdentifier());
					send(errorMsg); 
				}finally{
					if (isPrimaryRecipient) return; //delivery to the original recipient failed. No need to loop over secondary recipients as well.
				}
				
			}else {
				Message errorMsg = PredefinedMessageHelper.createDeliveryErrorMessage(msg, "Recipient type not supported", getMyIdentifier());
				send(errorMsg); 
				if (isPrimaryRecipient) return; //delivery to the original recipient failed. No need to loop over secondary recipients as well.
			}
			isPrimaryRecipient = false; //should hold true just for 1st loop, i.e., for the original receiver.
			
		}//end for loop
		
		return;
	}// end handleMessage();
	

	private void deliverToCollective(Message msg, Identifier recipient, boolean createDataStruct) throws NoSuchCollectiveException {
		CollectiveInfo colInfo = collectiveInfoProvider.getCollectiveInfo(recipient);
		if (createDataStruct){
			registerCollectiveMessageDeliveryAttempt(msg, colInfo);
		}
		
		for (Identifier peer : colInfo.getPeers()){
			try {
                Message localMessage = msg.clone();
                localMessage.setReceiverId(peer);
				deliverToPeer(localMessage, peer, createDataStruct, true);
			} catch (Exception e) {
				
				//Identifier origSender = msg.getSenderId();
				//Identifier origReceiver = msg.getReceiverId();
				
				//Message m = PredefinedMessageHelper.createDeliveryErrorMessage(msg, "Delivery to peer " + peer.getId() + " failed.", msg.getSenderId());
				Message m = PredefinedMessageHelper.createCommunicationErrorMessage(msg, "Delivery to peer " + peer.getId() + " failed.");
				//m.setReceiverId(m.getSenderId()); //just because receiver field will be later read to determine the sender (we are mimicking a message received from adapter), i.e. the part of the composite key to look up in the datastructure
				enforceCollectiveDeliveryPolicy(m);
				if (colInfo.getDeliveryPolicy() == DeliveryPolicy.Collective.TO_ALL_MEMBERS){ 
					break;
				}
			}
		}
	}
	
	private void deliverToPeer(final Message msg, Identifier recipient, boolean writeToDataStruct, boolean isIntendedForCollective)
			throws CommunicationException, NoSuchPeerException, RoutingException {
		
		boolean doSend = true;
		
		PeerInfo peerInfo = peerInfoProvider.getPeerInfo(recipient);

        if (peerInfo.getPrivacyPolicies() != null) {
            for (PrivacyPolicy privacyPolicy : peerInfo.getPrivacyPolicies()) {
                if (!privacyPolicy.condition(msg)) {
                    doSend = false;
                    break;
                }
            }
        }

		if (!doSend){
			//behave as if peer delivery policy failed
			throw new CommunicationException(new ErrorCode(1, "Delivery to peer " + recipient.getId() + "failed due to peer's privacy policy."));
		}
		
		if (writeToDataStruct){
			registerPeerMessageDeliveryAttempt(msg, peerInfo, isIntendedForCollective);
		}
		
		//returned values will be according to the peer delivery policy (e.g., only one ID for DeliveryPolicy.PREFERRED)
		final List<Identifier> listOfAdapterIDs = determineAdapters(msg, peerInfo);
		if (listOfAdapterIDs.size() == 0){
			throw new RoutingException(new ErrorCode(2, "Unable to determine the adapter for message delivery to peer " + peerInfo.getId()));
		}
		
		if (!writeToDataStruct && PredefinedMessageHelper.ACK_SUBTYPE.equals(msg.getSubtype())){
			msg.setSubtype(""); //because writeToDataStruct = false means this is not a primary receiver. Therefore, no need to ACK.
			
		}

        brokerExecutor.submit(new Runnable() {

            @Override
            public void run() {
                for (Identifier adapter : listOfAdapterIDs){
                    broker.publishOutput(adapter, msg);
                }
            }
        });

		
		
	}
	
	boolean collectiveDeliveryPolicyHasEntry(Message msg){
		
		//return collectiveDatastruct.containsKey(new DeliveryPolicyDataStructureKey(msg.getId(), msg.getSenderId()));
		return collectiveDatastruct.containsKey(new DeliveryPolicyDataStructureKey(msg.getRefersTo(), msg.getReceiverId()));
	}
	
	
	/**
	 * Takes the provided PeerInfo argument and determines whether there have been previous attempts to send messages to this peer.
	 * If not, then appropriate adapters will be created for the provided channels in PeerInfo and their Identifiers returned.
	 * If yes, then the method performs a deep comparison PeerInfo.equalsByDeepCoparison() to see if the stored instance of PeerInfo 
	 * differs from the new one. If they differ, then the AdapterManager's createEndpointForPeer() method is invoked anew,
	 * to create potentially missing adapters, and dispose those that will not be necessary anymore. The new PeerInfo instance is
	 * saved for subsequent checks. The resulting list of adapter identifiers is further restricted based on the peer delivery policy
	 * found in the provided PeerInfo parmeter.
	 * 
	 * @param msg
	 * @param newPeerInfo
	 * @return The list of adapters to which to perform delivery. 
	 */
	private List<Identifier> determineAdapters(Message msg, PeerInfo newPeerInfo){
		
		if (peerToAdaptersMappings.size() > MAP_SIZE_LIMIT) peerToAdaptersMappings.clear();
		
		if (peerToAdaptersMappings.containsKey(newPeerInfo)){ //uses just peer id comparison here. 
			//this is a known recipient
			//However, we still need to check if the available list of adapters (routing list) is valid.
			
			Pair<List<Identifier>, PeerInfo> p;
			p = peerToAdaptersMappings.get(newPeerInfo);
			
			int i = 1;
			while (p == null && i<4) { //this can happen if another thread flushed the entry in the meantime. That same thread should also update the value, though
				try {
					Thread.sleep(i*20);
				} catch (InterruptedException ignored) {}
				p = peerToAdaptersMappings.get(newPeerInfo);
				i++;
			}
			
			if (i >= 4 && p == null){
				//the other thread obviously did not do the job yet. We will take care of it in the rest of the method.
			}else{
				PeerInfo oldPeerInfo = p.second;
				if (newPeerInfo.equalsByDeepCoparison(oldPeerInfo)){
					return p.first; //return the existing list of adapters, since nothing changed in the PeerInfo from last time
				}
			}
		} 
		
		// if we arrived here it means, either:
		// sending message to this peer for the first time 
		// OR
		// the information we have is outdated, so let's update it
		

		List<Identifier> adapters = adapterMgr.createEndpointForPeer(newPeerInfo); //need not be synced, since invoking createEndpointForPeer multiple times should be fine
		Pair<List<Identifier>, PeerInfo> p = new Pair<List<Identifier>, PeerInfo>(adapters, newPeerInfo);
		
		synchronized(peerToAdaptersMappings){ //just using ConcurrentHashMap is not enough, because we need to save the reference to the map's key in the second object of the Pair, which is the object of the map, and if 2 thread repeat put, the value will get updated, but that value will contain the wrong reference to the key, meaning that subsequently the deep comparison between the old and the new PeerInfo object will not be possible
			peerToAdaptersMappings.remove(newPeerInfo); //in reality, we remove an existing entry, if any. Since the PeerInfo.equals just compares Ids, it should locate the exact entry.
			peerToAdaptersMappings.put(newPeerInfo, p); //we now put in the new value, which is a Pair, whose second element points to the key of the entry in the peerToAdaptersMappings where the Pair value belongs
		}
		
		
		// Now we have the list of all possible Adapters trough which we can deliver the message to the peer.
		// However, depending on peer's stated delivery policy we may want to restrict this list.
		// For example, in case of DeliveryPolicy.PREFERRED, we will just return the first Adapter.
		// But, in case of either TO_ALL_CHANNELS or AT_LEAST_ONE, we return all adapters, because in either case
		// the policy foresees sending to multiple adapters, but interpreting responses from all/one channel as ultimate success, respectively. 
		
		
		if (newPeerInfo.getDeliveryPolicy() == DeliveryPolicy.Peer.PREFERRED){
			Identifier preferred = adapters.get(0);
			List<Identifier> prefList = new ArrayList<Identifier>();
			prefList.add(preferred);
			return prefList;
			
		}
		
		return adapters;
	}
	
	boolean isMessagePartOfOriginalCollectiveDelivery(Message msg) {
	
		Identifier msgID_keypart = msg.getRefersTo();
		Identifier senderID_keypart = msg.getReceiverId();
		
		DeliveryPolicyDataStructureKey key = new DeliveryPolicyDataStructureKey(msgID_keypart, senderID_keypart);
		if (!peerDatastruct.containsKey(key)) return true; //if entry is not there, it was evicted by the response message fulfilling the collective policy
		
		ArrayList<PeerPolicyDataStructureValueElement> valEls = peerDatastruct.get(key);
		if (valEls == null || valEls.size() == 0) return true; //someone else just purged the datastructure, so return true, as it will send the ACK
		
		return valEls.get(0).isIntendedForCollective; //just reading from any element is enough
		
	}
	
	/**
	 * Invalidates a particular entry from the peer delivery data structure.
	 * Returns false immediately if there is no such entry, otherwise evicts and returns true. 
	 * Covers the case that another thread deleted the entry in the meantime
	 * @param msg
     * @throws Exception 
	 */
	boolean discardEntryInPeerDeliveryPolicyDataStructure(Message msg) {

		Identifier msgID_keypart = msg.getRefersTo();
		Identifier senderID_keypart = msg.getReceiverId();
		
		DeliveryPolicyDataStructureKey key = new DeliveryPolicyDataStructureKey(msgID_keypart, senderID_keypart);
		if (!peerDatastruct.containsKey(key)) return false;
		//Although here a race-condition may happen, since HashMap.remove is idempotent, 
		//so trying to remove again will make no harm. Returning true will just cost another check in the collectiveDeliveryDatastructure
		
		ArrayList<PeerPolicyDataStructureValueElement> valEls = peerDatastruct.get(key);
		
		if (valEls == null || valEls.size() == 0) return false;
		
		boolean returnVal = false;
		
		int n, i;
		synchronized (valEls){
			n = valEls.size();
			i = 0;
			for (PeerPolicyDataStructureValueElement e : valEls){
				if (e.peer.getId().equals(msg.getSenderId().getId())) {
					e.valid = false; //invalidate that particular entry
					returnVal = true;
				}
				if (!e.valid) i++;
			}

		}
		if (n == i){ //evict all
			peerDatastruct.remove(key);
		}
		
		//returnVal should be TRUE here. Otherwise, an incorrect msg.getSenderId() was supplied
		if(!returnVal) {
			log.debug("Unclean attempt at evicting the peer entry: <{},{},{}> ", msgID_keypart.toString(), senderID_keypart.toString(), msg.getSenderId().toString());
		}
		
		return returnVal;
	}
	
	
	/**
	 * Evicts all the entries 
	 * Returns false immediately if there is no such entry, otherwise evicts and returns true. 
	 * Covers the case that another thread deleted the entry in the meantime
	 * @param msg
	 */
	boolean discardAllCorrespondingEntriesInPeerDeliveryPolicyDataStructure(Message msg){

		Identifier msgID_keypart = msg.getRefersTo();
		Identifier senderID_keypart = msg.getReceiverId();
		
		DeliveryPolicyDataStructureKey key = new DeliveryPolicyDataStructureKey(msgID_keypart, senderID_keypart);
		if (!peerDatastruct.containsKey(key)) return false;
		//Although here a race-condition may happen, since HashMap.remove is idempotent, 
		//so trying to remove again will make no harm. Returning true will just cost another check in the collectiveDeliveryDatastructure
		
		ArrayList<PeerPolicyDataStructureValueElement> valEls = peerDatastruct.get(key);
		
		if (valEls == null || valEls.size() == 0) return false;
				
		peerDatastruct.remove(key);
		
		synchronized (valEls){
			for (PeerPolicyDataStructureValueElement e : valEls){
				e.valid = false;
			}
		}
		return true;
	}
	
	void discardCollectivePolicyEntry(Identifier msgID_keypart, Identifier senderID_keypart){
		collectiveDatastruct.remove(new DeliveryPolicyDataStructureKey(msgID_keypart, senderID_keypart));
	}
	
	/**
	 * Invoked asynchronously when a control message is received from the broker indicating ACK/ERR, and determine whether
	 * the collective delivery policy still holds.
	 * @param msg -- message containing the original sender (not the one who replies with ACK/ERR) and the refersTo field indicating the original message. @see createDeliveryErrorMessage()
	 * @return return true if policy conclusively succeeded (e.g., 1 message successfully delivered, and TO_ANY); false if still valid but still not succeeded, or entry not found; Exception if conclusively failed.
	 * @throws Exception if it is conclusively clear that the policy will not be upheld (e.g., 1 delivery failed for TO_ALL)
	 */
	boolean checkCollectiveDeliveryPolicy(Message msg) throws Exception {

		DeliveryPolicy policy = collectiveDatastruct.get(new DeliveryPolicyDataStructureKey(msg.getRefersTo(), msg.getReceiverId()));

		if (policy == null) {
			return false;
		}
		
		if (PredefinedMessageHelper.ACK_SUBTYPE.equals(msg.getSubtype())){
			return policy.check(DeliveryPolicy.CHECK_ACK);
		}else{
			return policy.check(DeliveryPolicy.CHECK_ERR); //can throw an exception (e.g., all sending attempts failed in case of DeliveryPolicy.Collective.TO_ANY)
		}
	}
	
	/**
	 * Invoked asynchronously when a control message is received from the broker indicating ACK/ERR, and determine whether
	 * the peer delivery policy still holds.
	 * @param msg -- message containing the original sender (not the one who replies with ACK/ERR) and the refersTo field indicating the original message. @see createDeliveryErrorMessage()
	 * @return return true if policy conclusively succeeded (e.g., 1 message successfully delivered, and AT_LEAST_ONE); false if still valid but still not succeeded, or entry not found; Exception if conclusively failed.
	 * @throws Exception if it is conclusively clear that the policy will not be upheld (e.g., 1 delivery failed for TO_ALL_CHANNELS)
	 */
	boolean checkPeerDeliveryPolicy(Message msg) throws Exception {
		
		ArrayList<PeerPolicyDataStructureValueElement> list = peerDatastruct.get(new DeliveryPolicyDataStructureKey(msg.getRefersTo(), msg.getReceiverId())); //we switch places of sender and receiver here, because the input param msg is the one received back prom input adapter, meaning that the sender is the original receiver, and vice versa
		
		if (list == null) {
			return false; //entry already evicted
		}
		
		PeerPolicyDataStructureValueElement searchedFor = null;
		synchronized(list){ //must be synchronized, in case another thread tries to write into the list
			for (PeerPolicyDataStructureValueElement el : list){ //should be fairly quick, considering the average size of a collective <100
				if (el.peer.equals(msg.getSenderId())) {
					searchedFor = el;
					break;
				}
			}
		}
		
		if (searchedFor == null) {
			return false; //entry already evicted
		}
		
		DeliveryPolicy policy = searchedFor.policy;
		
		if (PredefinedMessageHelper.ACK_SUBTYPE.equals(msg.getSubtype())){
			return policy.check(DeliveryPolicy.CHECK_ACK);
		}else{
			return policy.check(DeliveryPolicy.CHECK_ERR); //can throw an exception (e.g., all sending attempts failed in case of DeliveryPolicy.Peer.AT_LEAST_ONE)
		}
		
		
	}
	
	
	

	
	public void handleComponentMessage(Message msg, Identifier receiver){
		//In general case, here we will decide how to deliver the message to a particular component.
		// Athe the moment, we just notify all registered callbacks
		notifyCallbacks(msg);

	}
	
	/**
	 * Determines the additional receivers for the message based on the routing rules in place.
	 * @param msg
	 * @return
	 */
	public Collection<Identifier> determineReceivers(Message msg){
		
		//if receiver is null, the msg may still be valid, e.g., CONTROL_TYPE +  ERROR_SUBTYPE, etc.
		// in this case we may want to inform through callback tee (in this case add its Identifier and handleComponentMessage() will take care of it) 

		HashSet<Identifier> receivers = new HashSet<Identifier>();
		
		//TODO Determine based on the routing table.
		
		if (msg.getType() == null || msg.getType().equals("")) {
			msg.setType(PredefinedMessageHelper.DATA_TYPE);
		}
		
		switch (msg.getType()){
			case PredefinedMessageHelper.CONTROL_TYPE:
				
				switch (msg.getSubtype()){
					case PredefinedMessageHelper.DELIVERY_ERROR_SUBTYPE:
					case PredefinedMessageHelper.ACK_SUBTYPE_CHECKED:
						receivers.add(PredefinedMessageHelper.taskExecutionEngine);
				}
				
				break;
			case PredefinedMessageHelper.DATA_TYPE:
			default:
				break;
		}
		
		return receivers;
	}
	
	/**
	 * 
	 * @param msg
	 * @param deliveryPolicyType
	 */
	private void registerCollectiveMessageDeliveryAttempt(Message msg, CollectiveInfo colInfo){
		
		switch (colInfo.getDeliveryPolicy()){
		case TO_ANY:
			collectiveDatastruct.put(new DeliveryPolicyDataStructureKey(msg.getId(), msg.getSenderId()), new SimpleToAnyCollectivePolicy(colInfo.getPeers().size()));
			break;
		
		case TO_ALL_MEMBERS:
		default:
			collectiveDatastruct.put(new DeliveryPolicyDataStructureKey(msg.getId(), msg.getSenderId()), new SimpleToAllCollectivePolicy(colInfo.getPeers().size()));
			break;
		}
	}
	
	private void registerPeerMessageDeliveryAttempt(Message msg, PeerInfo peerInfo, boolean isIntendedForCollective){
		
		
		DeliveryPolicy.Peer deliveryPolicyType = peerInfo.getDeliveryPolicy();
		
		DeliveryPolicy deliveryPolicy;
		
		switch (deliveryPolicyType){
			case AT_LEAST_ONE:
				deliveryPolicy = new SimpleAtLeastOnePeerPolicy(peerInfo);
				break;
			case PREFERRED:
				deliveryPolicy = new SimplePreferredPeerPolicy();
				break;
			case TO_ALL_CHANNELS:
			default:
				deliveryPolicy = new SimpleToAllChannelsPeerPolicy(peerInfo);
				break;
		}

		DeliveryPolicyDataStructureKey key = new DeliveryPolicyDataStructureKey(msg.getId(), msg.getSenderId());
		PeerPolicyDataStructureValueElement valEl = new PeerPolicyDataStructureValueElement(msg.getReceiverId(), deliveryPolicy, isIntendedForCollective);
		
		ArrayList<PeerPolicyDataStructureValueElement> values = null;
		
		if (peerDatastruct.containsKey(key)){
			values = peerDatastruct.get(key);
				if (values == null) { //in a rare case a message to a peer from a collective has not been sent yet, while ACK from another one arrived with TO_ANY, and the corresponding peer entry got purged just after we checked that it existed...
					return;
				}
				synchronized(values){
					values.add(valEl);
				}
		}else{
			synchronized(peerDatastruct){
				if (peerDatastruct.containsKey(key)){ //just in case another thread created the entry in the meantime. Should not happen often
					values = peerDatastruct.get(key);
					values.add(valEl);
				}else{
					values = new ArrayList<PeerPolicyDataStructureValueElement>();
					values.add(valEl);
					peerDatastruct.put(key, values);
				}
			}
		}
		
		
	}
	
    /**
     * Generate an unique id
     * @return unique id
     */
    private String generateUniqueIdString() {
        return TimeBasedUUID.getUUIDAsString();
    }
    
    /**
     * Returns the {@link Identifier}  of the instance from which the method got invoked
     * @return 
     */
    public Identifier getMyIdentifier(){
    	return Identifier.component(this.hashCode() + "");
    }
    
    
    //////////////////////////
    
    /**
     * PLays the role of key for both Collective- and Peer data structure
     *
     */
    public class DeliveryPolicyDataStructureKey {
    	public final String msgID_keypart;
    	public final String senderID_keypart;
    	
    	public DeliveryPolicyDataStructureKey (Identifier msgIdentifier, Identifier senderIdentifier){
    		this.msgID_keypart = msgIdentifier.getId();
    		this.senderID_keypart = senderIdentifier.getId();
    	}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((msgID_keypart == null) ? 0 : msgID_keypart.hashCode());
			result = prime
					* result
					+ ((senderID_keypart == null) ? 0 : senderID_keypart
							.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DeliveryPolicyDataStructureKey other = (DeliveryPolicyDataStructureKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (msgID_keypart == null) {
				if (other.msgID_keypart != null)
					return false;
			} else if (!msgID_keypart.equals(other.msgID_keypart))
				return false;
			if (senderID_keypart == null) {
				if (other.senderID_keypart != null)
					return false;
			} else if (!senderID_keypart.equals(other.senderID_keypart))
				return false;
			return true;
		}

		private MessagingAndRoutingManagerImpl getOuterType() {
			return MessagingAndRoutingManagerImpl.this;
		}
    	
    }
    
    public class PeerPolicyDataStructureValueElement{
    	public final Identifier peer;
    	public final DeliveryPolicy policy;
    	public boolean valid;
    	public boolean isIntendedForCollective;
    	
    	public PeerPolicyDataStructureValueElement(Identifier peer, DeliveryPolicy policy, boolean forCollective){
    		this.peer = peer;
    		this.policy = policy;
    		this.valid = true;
    		this.isIntendedForCollective = forCollective;
    	}
    }

	/**
	 * Takes as input the ACK/error message. The method looks up the record in the appropriate data structure, 
	 * and locates the handler that will check whether the collective delivery
	 * policy still holds, and based on that outcome will enforce the policy. "Enforcing" means that if the policy
	 * can be considered as failed, corresponding entries from the data structures will be deleted, 
	 * and appropriate messages sent out. 
	 * @param msg -- response message received from and input adapter, containing: sender:=adapter/peer(original receiver), receiver:=original sender, refersTo:=original message id
	 */
	void enforceCollectiveDeliveryPolicy(Message msg){ 
		//from the message obtained from msg.getRefersTo() get the message id, while from msg.getSenderId() we get the sender to look up the entry in the data structure
		try{
			boolean policySucceeded = checkCollectiveDeliveryPolicy(msg); //return true if policy conclusively succeeded, false if still valid but still not succeeded, Exception if conclusively failed. 
			if (policySucceeded){ //collective policy conclusively succeeded. Purge entries from corresponding data structures.
				//collectiveDiscardCondition.lock(); //with the concurrent map should also work without this
				discardAllCorrespondingEntriesInPeerDeliveryPolicyDataStructure(msg);
				discardCollectivePolicyEntry(msg.getRefersTo(), msg.getReceiverId());
				
				if (msg.isWantsAcknowledgement()){
					handleMessage(PredefinedMessageHelper.createAcknowledgeMessageFromAdaptersAcknowledgeMessage(msg)); 
				}
			}
		}catch(Exception e){ //collective policy conclusively failed. Purge entries, and inform the original sender of the ERR.
			//if (!collectiveDiscardCondition.isHeldByCurrentThread()) collectiveDiscardCondition.lock();
			
			discardAllCorrespondingEntriesInPeerDeliveryPolicyDataStructure(msg);
			discardCollectivePolicyEntry(msg.getRefersTo(), msg.getReceiverId());
			
			//this implies that I want the broker to deliver me messages which will have: 
			//sender=the actual peer that failed/succeeded, receiver: the component who originally sent the message to which they are replying to
			//plus refersTo field, containing the id of the original message 
			handleMessage(PredefinedMessageHelper.createDeliveryErrorMessageFromAdaptersCommunicationErrorMessage(msg, "Collective delivery policy failed."));  
		}finally{
			//collectiveDiscardCondition.unlock();
		}

	}
	
	private void enforcePeerDeliveryPolicy(Message msg){ 
		try {
			boolean policySucceeded = checkPeerDeliveryPolicy(msg);
			 
			if (policySucceeded){
				boolean isMessagePartOfOriginalCollectiveDelivery = isMessagePartOfOriginalCollectiveDelivery(msg);
				if (!discardEntryInPeerDeliveryPolicyDataStructure(msg)){
					return; //returns if there was no such entry. 
				}
				
				//there was a peer entry, which was invalidated, or even whole key+value removed.
				if (collectiveDeliveryPolicyHasEntry(msg)){
					enforceCollectiveDeliveryPolicy(msg);
				}else{
					if (isMessagePartOfOriginalCollectiveDelivery) return; //if the original message was for collective and there is no collective entry anymore, then no ACKs should be sent, because it should have been sent already
					
					if (msg.isWantsAcknowledgement()){
						handleMessage(PredefinedMessageHelper.createAcknowledgeMessageFromAdaptersAcknowledgeMessage(msg));
					}
					
				}
			}else{
				//nothing, policy still inconclusive
			}
		}catch(Exception e){ //policy failed
			if (!discardEntryInPeerDeliveryPolicyDataStructure(msg)){
				return;
			}
			if (collectiveDeliveryPolicyHasEntry(msg)){
				enforceCollectiveDeliveryPolicy(msg);
			}else{
				//send ERR to TEE
				Message errMsg = PredefinedMessageHelper.createDeliveryErrorMessageFromAdaptersCommunicationErrorMessage(msg, "Peer delivery policy failed.");
				handleMessage(errMsg);
			}
		}
	}

    @Override
	public void onMessage(final Message msg) {
        if (PredefinedMessageHelper.CONTROL_TYPE.equals(msg.getType())) {
            statistic.controlReceived();
        } else {
            statistic.inputReceived();
        }
        send(msg);
	}
}// end class MessagingAndRoutingManager
