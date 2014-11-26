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
package at.ac.tuwien.dsg.smartcom.manager.messaging;

import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.callback.CollectiveInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.exception.ErrorCode;
import at.ac.tuwien.dsg.smartcom.exception.InvalidRuleException;
import at.ac.tuwien.dsg.smartcom.exception.RoutingException;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.MessagingAndRoutingManager;
import at.ac.tuwien.dsg.smartcom.manager.messaging.util.KeyProvider;
import at.ac.tuwien.dsg.smartcom.manager.messaging.util.Pair;
import at.ac.tuwien.dsg.smartcom.manager.messaging.util.TimeBasedUUID;
import at.ac.tuwien.dsg.smartcom.model.*;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import at.ac.tuwien.dsg.smartcom.utils.PredefinedMessageHelper;
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
public class MessagingAndRoutingManagerImpl implements MessagingAndRoutingManager {
	private static final Logger log = LoggerFactory.getLogger(MessagingAndRoutingManager.class);
	
	private final static int MAP_SIZE_LIMIT = 20000;

	private ExecutorService executor;
    private ExecutorService brokerExecutor;
    private ExecutorService logExecutor;

	private final Map<Identifier, NotificationCallback> callbacks = new HashMap<>();

	private final Map<PeerInfo, Pair<List<Identifier>, PeerInfo>>  peerToAdaptersMappings = new ConcurrentHashMap<>();

	@Inject
	private PeerInfoService peerInfoProvider;
	
	@Inject
	private CollectiveInfoCallback collectiveInfoProvider;
		
	@Inject
    private MessageBroker broker; 
	
	@Inject
	private AdapterManager adapterMgr;

    @Inject
    private StatisticBean statistic;

	@Inject
	private MessageLogLevel logLevel;

    private final Identifier localId = Identifier.component(this.hashCode() + "");

	private InputHandler inputHandler;
	private RoutingRuleEngine routingRuleEngine;
	private PolicyEnforcer policyEnforcer;

	/**
    * Initializes the executors for message sending.
    */
	@PostConstruct
    public void init() {
    	ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("MARM-thread-%d").build();
    	executor = Executors.newFixedThreadPool(10, namedThreadFactory);

        ThreadFactory brokerThreadFactory = new ThreadFactoryBuilder().setNameFormat("Broker-thread-%d").build();
        brokerExecutor = Executors.newFixedThreadPool(10, brokerThreadFactory);

        ThreadFactory logThreadFactory = new ThreadFactoryBuilder().setNameFormat("LOG-thread-%d").setPriority(Thread.MIN_PRIORITY).build();
        logExecutor = Executors.newFixedThreadPool(10, logThreadFactory);

		inputHandler = new InputHandler(this, broker, statistic);
		inputHandler.init();

		routingRuleEngine = new RoutingRuleEngine();
		policyEnforcer = new PolicyEnforcer(this);
    }

    /**
     * Stops the executors for and cancels all active Futures.
     */
	@PreDestroy
    public void destroy() {
        log.info("Executor will be shut down");

		inputHandler.destroy();
		routingRuleEngine.clear();

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
			message.setId(new Identifier(IdentifierType.MESSAGE, KeyProvider.generateUniqueIdString(), ""));
		}else{
			log.warn("Message with a pre-set ID {} received. Discarding.", message.getId().getId());
			return null;
		}

        log.trace("Received message: {}", message);
		logExecutor.submit(new Runnable() {

			@Override
			public void run() {
				switch (logLevel) {
					case ALL:
						log();
						break;
					case INGOING:
						if (message.getSenderId() != null) {
							log();
						}
						break;
					case OUTGOING:
						if (message.getSenderId() == null) {
							log();
						}
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
						//do nothing
				}
			}

			private void log() {
				statistic.logRequest();
				broker.publishLog(message.clone());
			}
		});

        executor.submit(
            new Runnable() {

                @Override
                public void run() {
                    if (PredefinedMessageHelper.CONTROL_TYPE.equals(message.getType()) &&
                            (PredefinedMessageHelper.ACK_SUBTYPE.equals(message.getSubtype()) ||
                            PredefinedMessageHelper.COMERROR_SUBTYPE.equals(message.getSubtype()))) {
                        statistic.internalMessageSendingRequest();
						policyEnforcer.enforcePeerDeliveryPolicy(message);
                    } else {
                        statistic.externalMessageSendingRequest();
						MessagingAndRoutingManagerImpl.this.handleMessage(message);
                    }
                }
					
        });
		//futureList.add(future); //we probably won't need this at all, as sending should mean ultimately just 
		//passing the msg to the appropriate broker. 
		//Therefore, it would be just wasting time to track futures just to be able to cancel them.
	
		return message.getId();
	}

	@Override
	public Identifier addRouting(RoutingRule rule) throws InvalidRuleException {
		return routingRuleEngine.addRouting(rule);
	}

	@Override
	public RoutingRule removeRouting(Identifier routeId) {
		return routingRuleEngine.removeRouting(routeId);
	}

	@Override
	public Identifier registerNotificationCallback(NotificationCallback callback) {
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
            callback.notify(msg);
        }
	}

	protected void handleMessage(Message msg)  {
		boolean isPrimaryRecipient = false;
		ArrayList<Identifier> receiverList = new ArrayList<>();
		Identifier receiver = msg.getReceiverId();

		if (msg.getReceiverId() != null){
			receiverList.add(receiver); //this is the original receiver
			isPrimaryRecipient = true;	//ex createDataStruct. For the original receiver we need to create the data structure to track delivery of messages
		}

		receiverList.addAll(determineReceivers(msg));

		if (receiverList.isEmpty()){
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
					Message errorMsg = PredefinedMessageHelper.createDeliveryErrorMessage(msg, "Attempted delivery to unknown or non-existent collective " + rec.getId() + ".", localId);
					send(errorMsg);
					if (isPrimaryRecipient) return; //delivery to the original recipient failed. No need to loop over secondary recipients as well.
				}
			}else if (rec.getType() == IdentifierType.PEER){
				try {
                    statistic.peerMessageSendingRequest();
					deliverToPeer(msg, rec, isPrimaryRecipient, false);
				} catch (CommunicationException e) {
					Message errorMsg = PredefinedMessageHelper.createCommunicationErrorMessage(msg, "Delivery to peer " + rec.getId() + " failed.");
					policyEnforcer.enforcePeerDeliveryPolicy(errorMsg);
				} catch (Exception e){ //in case the peer was not found or adapter could not be instantiated
					Message errorMsg = PredefinedMessageHelper.createDeliveryErrorMessage(msg, "Delivery to peer " + rec.getId() + " failed.", localId);
					send(errorMsg);
				}

				if (isPrimaryRecipient) return; //delivery to the original recipient failed. No need to loop over secondary recipients as well.
			}else {
				Message errorMsg = PredefinedMessageHelper.createDeliveryErrorMessage(msg, "Recipient type not supported", localId);
				send(errorMsg);
				if (isPrimaryRecipient) return; //delivery to the original recipient failed. No need to loop over secondary recipients as well.
			}
			isPrimaryRecipient = false; //should hold true just for 1st loop, i.e., for the original receiver.

		}//end for loop
	}

	private void deliverToCollective(Message msg, Identifier recipient, boolean createDataStruct) throws NoSuchCollectiveException {
		CollectiveInfo colInfo = collectiveInfoProvider.getCollectiveInfo(recipient);
		if (createDataStruct){
			policyEnforcer.registerCollectiveMessageDeliveryAttempt(msg, colInfo);
		}

		for (Identifier peer : colInfo.getPeers()){
			try {
                Message localMessage = msg.clone();
                localMessage.setReceiverId(peer);
				deliverToPeer(localMessage, peer, createDataStruct, true);
			} catch (Exception e) {

				Message m = PredefinedMessageHelper.createCommunicationErrorMessage(msg, "Delivery to peer " + peer.getId() + " failed.");
				policyEnforcer.enforceCollectiveDeliveryPolicy(m);
				if (colInfo.getDeliveryPolicy() == DeliveryPolicy.Collective.TO_ALL_MEMBERS){
					break;
				}
			}
		}
	}
	
	private void deliverToPeer(final Message msg, Identifier recipient, boolean writeToDataStruct, boolean isIntendedForCollective) throws CommunicationException, NoSuchPeerException {

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
			policyEnforcer.registerPeerMessageDeliveryAttempt(msg, peerInfo, isIntendedForCollective);
		}

		//returned values will be according to the peer delivery policy (e.g., only one ID for DeliveryPolicy.PREFERRED)
		final List<Identifier> listOfAdapterIDs = determineAdapters(peerInfo);
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

	/**
	 * Takes the provided PeerInfo argument and determines whether there have been previous attempts to send messages to this peer.
	 * If not, then appropriate adapters will be created for the provided channels in PeerInfo and their Identifiers returned.
	 * If yes, then the method performs a deep comparison PeerInfo.equalsByDeepCoparison() to see if the stored instance of PeerInfo 
	 * differs from the new one. If they differ, then the AdapterManager's createEndpointForPeer() method is invoked anew,
	 * to create potentially missing adapters, and dispose those that will not be necessary anymore. The new PeerInfo instance is
	 * saved for subsequent checks. The resulting list of adapter identifiers is further restricted based on the peer delivery policy
	 * found in the provided PeerInfo parmeter.
	 *
	 * @param peerInfo information of a peer
	 * @return The list of adapters to which to perform delivery. 
	 */
	private List<Identifier> determineAdapters(PeerInfo peerInfo){
		
		if (peerToAdaptersMappings.size() > MAP_SIZE_LIMIT) peerToAdaptersMappings.clear();
		
		if (peerToAdaptersMappings.containsKey(peerInfo)){ //uses just peer id comparison here.
			//this is a known recipient
			//However, we still need to check if the available list of adapters (routing list) is valid.
			
			Pair<List<Identifier>, PeerInfo> p;
			p = peerToAdaptersMappings.get(peerInfo);
			
			int i = 1;
			while (p == null && i<4) { //this can happen if another thread flushed the entry in the meantime. That same thread should also update the value, though
				try {
					Thread.sleep(i*20);
				} catch (InterruptedException ignored) {}
				p = peerToAdaptersMappings.get(peerInfo);
				i++;
			}

			if (i < 4 && p != null) {
				PeerInfo oldPeerInfo = p.second;
				if (peerInfo.equalsByDeepCoparison(oldPeerInfo)){
					return p.first; //return the existing list of adapters, since nothing changed in the PeerInfo from last time
				}
			}
		} 
		
		// if we arrived here it means, either:
		// sending message to this peer for the first time 
		// OR
		// the information we have is outdated, so let's update it
		

		List<Identifier> adapters = adapterMgr.createEndpointForPeer(peerInfo); //need not be synced, since invoking createEndpointForPeer multiple times should be fine
		Pair<List<Identifier>, PeerInfo> p = new Pair<>(adapters, peerInfo);
		
		synchronized(peerToAdaptersMappings){ //just using ConcurrentHashMap is not enough, because we need to save the reference to the map's key in the second object of the Pair, which is the object of the map, and if 2 thread repeat put, the value will get updated, but that value will contain the wrong reference to the key, meaning that subsequently the deep comparison between the old and the new PeerInfo object will not be possible
			peerToAdaptersMappings.remove(peerInfo); //in reality, we remove an existing entry, if any. Since the PeerInfo.equals just compares Ids, it should locate the exact entry.
			peerToAdaptersMappings.put(peerInfo, p); //we now put in the new value, which is a Pair, whose second element points to the key of the entry in the peerToAdaptersMappings where the Pair value belongs
		}
		
		
		// Now we have the list of all possible Adapters trough which we can deliver the message to the peer.
		// However, depending on peer's stated delivery policy we may want to restrict this list.
		// For example, in case of DeliveryPolicy.PREFERRED, we will just return the first Adapter.
		// But, in case of either TO_ALL_CHANNELS or AT_LEAST_ONE, we return all adapters, because in either case
		// the policy foresees sending to multiple adapters, but interpreting responses from all/one channel as ultimate success, respectively. 
		
		
		if (peerInfo.getDeliveryPolicy() == DeliveryPolicy.Peer.PREFERRED){
			Identifier preferred = adapters.get(0);
			List<Identifier> prefList = new ArrayList<>();
			prefList.add(preferred);
			return prefList;
			
		}
		
		return adapters;
	}

	public void handleComponentMessage(final Message msg, Identifier receiver){
		if (PredefinedMessageHelper.AUTH_TYPE.equals(msg.getType())) {
			brokerExecutor.submit(new Runnable() {

				@Override
				public void run() {
					broker.publishAuthRequest(msg);
				}
			});
		} else {
			notifyCallbacks(msg);
		}
	}
	
	/**
	 * Determines the additional receivers for the message based on the routing rules in place. If receiver is
	 * null, the msg may still be valid, e.g., CONTROL_TYPE +  ERROR_SUBTYPE, etc.
	 * In this case the callback will be informed.
	 * @param msg message
	 * @return list of receivers
	 */
	private Collection<Identifier> determineReceivers(Message msg){
		Set<Identifier> receivers = new HashSet<>();

		receivers.addAll(routingRuleEngine.performRouting(msg));

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
}
