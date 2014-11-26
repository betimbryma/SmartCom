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

import at.ac.tuwien.dsg.smartcom.manager.messaging.policies.delivery.collective.SimpleToAllCollectivePolicy;
import at.ac.tuwien.dsg.smartcom.manager.messaging.policies.delivery.collective.SimpleToAnyCollectivePolicy;
import at.ac.tuwien.dsg.smartcom.manager.messaging.policies.delivery.peer.SimpleAtLeastOnePeerPolicy;
import at.ac.tuwien.dsg.smartcom.manager.messaging.policies.delivery.peer.SimplePreferredPeerPolicy;
import at.ac.tuwien.dsg.smartcom.manager.messaging.policies.delivery.peer.SimpleToAllChannelsPeerPolicy;
import at.ac.tuwien.dsg.smartcom.model.*;
import at.ac.tuwien.dsg.smartcom.utils.PredefinedMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @author Ognjen Scekic
 * @version 1.0
 */
public class PolicyEnforcer {
    private static final Logger log = LoggerFactory.getLogger(PolicyEnforcer.class);

    private final static int COL_DS_SIZE = 1000;
    private final static int PEER_DS_SIZE = 10 * COL_DS_SIZE;

    private final Map<DeliveryPolicyDataStructureKey, DeliveryPolicy> collectiveDatastruct = new ConcurrentHashMap<>(COL_DS_SIZE);
    private final Map<DeliveryPolicyDataStructureKey, List<PeerPolicyDataStructureValueElement>> peerDatastruct = new ConcurrentHashMap<>(PEER_DS_SIZE);

    private final MessagingAndRoutingManagerImpl marm;

    public PolicyEnforcer(MessagingAndRoutingManagerImpl marm) {
        this.marm = marm;
    }

    /**
     *
     * @param msg message
     * @param colInfo collective info
     */
    public void registerCollectiveMessageDeliveryAttempt(Message msg, CollectiveInfo colInfo){

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

    public void registerPeerMessageDeliveryAttempt(Message msg, PeerInfo peerInfo, boolean isIntendedForCollective){
        DeliveryPolicy.Peer deliveryPolicyType = peerInfo.getDeliveryPolicy();
        DeliveryPolicy deliveryPolicy;

        switch (deliveryPolicyType) {
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

        List<PeerPolicyDataStructureValueElement> values = peerDatastruct.get(key);

        if (values == null) {
            synchronized(peerDatastruct) {
                values = peerDatastruct.get(key);
                if (values == null) {
                    values = Collections.synchronizedList(new ArrayList<PeerPolicyDataStructureValueElement>());
                    peerDatastruct.put(key, values);
                }
            }
        }

        values.add(valEl);
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

                if (DeliveryPolicy.Message.ACKNOWLEDGED.equals(msg.getDelivery())) {
                    marm.handleMessage(PredefinedMessageHelper.createAcknowledgeMessageFromAdaptersAcknowledgeMessage(msg));
                }
            }
        }catch(Exception e){ //collective policy conclusively failed. Purge entries, and inform the original sender of the ERR.
            //if (!collectiveDiscardCondition.isHeldByCurrentThread()) collectiveDiscardCondition.lock();

            discardAllCorrespondingEntriesInPeerDeliveryPolicyDataStructure(msg);
            discardCollectivePolicyEntry(msg.getRefersTo(), msg.getReceiverId());

            //this implies that I want the broker to deliver me messages which will have:
            //sender=the actual peer that failed/succeeded, receiver: the component who originally sent the message to which they are replying to
            //plus refersTo field, containing the id of the original message
            marm.handleMessage(PredefinedMessageHelper.createDeliveryErrorMessageFromAdaptersCommunicationErrorMessage(msg, "Collective delivery policy failed."));
        }
    }

    public void enforcePeerDeliveryPolicy(Message msg) {
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

                    if (DeliveryPolicy.Message.ACKNOWLEDGED.equals(msg.getDelivery())){
                        marm.handleMessage(PredefinedMessageHelper.createAcknowledgeMessageFromAdaptersAcknowledgeMessage(msg));
                    }

                }
            }
        } catch(Exception e) { //policy failed
            if (!discardEntryInPeerDeliveryPolicyDataStructure(msg)){
                return;
            }
            if (collectiveDeliveryPolicyHasEntry(msg)){
                enforceCollectiveDeliveryPolicy(msg);
            }else{
                //send ERR to TEE
                Message errMsg = PredefinedMessageHelper.createDeliveryErrorMessageFromAdaptersCommunicationErrorMessage(msg, "Peer delivery policy failed.");
                marm.handleMessage(errMsg);
            }
        }
    }

    boolean collectiveDeliveryPolicyHasEntry(Message msg){
        return collectiveDatastruct.containsKey(new DeliveryPolicyDataStructureKey(msg.getRefersTo(), msg.getReceiverId()));
    }

    boolean isMessagePartOfOriginalCollectiveDelivery(Message msg) {

        Identifier msgID_keypart = msg.getRefersTo();
        Identifier senderID_keypart = msg.getReceiverId();

        DeliveryPolicyDataStructureKey key = new DeliveryPolicyDataStructureKey(msgID_keypart, senderID_keypart);
        if (!peerDatastruct.containsKey(key))
            return true; //if entry is not there, it was evicted by the response message fulfilling the collective policy

        List<PeerPolicyDataStructureValueElement> valEls = peerDatastruct.get(key);
        //someone else just purged the data structure, so return true, as it will send the ACK
        return (valEls == null || valEls.size() == 0 || valEls.get(0).isIntendedForCollective);
    }

    /**
     * Invalidates a particular entry from the peer delivery data structure.
     * Returns false immediately if there is no such entry, otherwise evicts and returns true.
     * Covers the case that another thread deleted the entry in the meantime
     *
     * @param msg message
     */
    boolean discardEntryInPeerDeliveryPolicyDataStructure(Message msg) {

        Identifier msgID_keypart = msg.getRefersTo();
        Identifier senderID_keypart = msg.getReceiverId();

        DeliveryPolicyDataStructureKey key = new DeliveryPolicyDataStructureKey(msgID_keypart, senderID_keypart);
        if (!peerDatastruct.containsKey(key)) return false;
        //Although here a race-condition may happen, since HashMap.remove is idempotent,
        //so trying to remove again will make no harm. Returning true will just cost another check in the collectiveDeliveryDatastructure

        List<PeerPolicyDataStructureValueElement> valEls = peerDatastruct.get(key);

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
     * @param msg message
     */
    boolean discardAllCorrespondingEntriesInPeerDeliveryPolicyDataStructure(Message msg){

        Identifier msgID_keypart = msg.getRefersTo();
        Identifier senderID_keypart = msg.getReceiverId();

        DeliveryPolicyDataStructureKey key = new DeliveryPolicyDataStructureKey(msgID_keypart, senderID_keypart);
        if (!peerDatastruct.containsKey(key)) return false;
        //Although here a race-condition may happen, since HashMap.remove is idempotent,
        //so trying to remove again will make no harm. Returning true will just cost another check in the collectiveDeliveryDatastructure

        List<PeerPolicyDataStructureValueElement> valEls = peerDatastruct.get(key);

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
        } else {
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

        List<PeerPolicyDataStructureValueElement> list = peerDatastruct.get(new DeliveryPolicyDataStructureKey(msg.getRefersTo(), msg.getReceiverId())); //we switch places of sender and receiver here, because the input param msg is the one received back prom input adapter, meaning that the sender is the original receiver, and vice versa

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
        } else {
            return policy.check(DeliveryPolicy.CHECK_ERR); //can throw an exception (e.g., all sending attempts failed in case of DeliveryPolicy.Peer.AT_LEAST_ONE)
        }
    }

    /**
     * Plays the role of key for both Collective- and Peer data structure
     *
     */
    public class DeliveryPolicyDataStructureKey {
        final String msgID_keypart;
        final String senderID_keypart;

        public DeliveryPolicyDataStructureKey (Identifier msgIdentifier, Identifier senderIdentifier){
            this.msgID_keypart = msgIdentifier.getId();
            this.senderID_keypart = senderIdentifier.getId();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
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
    }

    public class PeerPolicyDataStructureValueElement{
        final Identifier peer;
        final DeliveryPolicy policy;
        boolean valid;
        boolean isIntendedForCollective;

        public PeerPolicyDataStructureValueElement(Identifier peer, DeliveryPolicy policy, boolean forCollective){
            this.peer = peer;
            this.policy = policy;
            this.valid = true;
            this.isIntendedForCollective = forCollective;
        }
    }
}
