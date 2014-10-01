package at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.peer;

import java.util.concurrent.atomic.AtomicInteger;

import at.ac.tuwien.dsg.smartcom.exception.DeliveryPolicyFailedException;
import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.AbstractDeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;

public class SimpleToAllChannelsPeerPolicy extends AbstractPeerDeliveryPolicy {
	
	private java.util.concurrent.atomic.AtomicInteger numOfSuccessfullDeliveries;
	private final int requiredNumberOfSuccessfulDeliveries;
	
	public SimpleToAllChannelsPeerPolicy(PeerInfo peerInfo){
		super("SimpleToAllChannelsPeerPolicy", peerInfo);
		requiredNumberOfSuccessfulDeliveries = peerInfo.getAddresses().size(); //get channels
		numOfSuccessfullDeliveries = new AtomicInteger(0);
	}
	
	public boolean check(int whatToCheck) throws DeliveryPolicyFailedException {
		
        if (whatToCheck == DeliveryPolicy.CHECK_ACK){
	        if (numOfSuccessfullDeliveries.incrementAndGet() == requiredNumberOfSuccessfulDeliveries){
	        	return true;
	        }else{
	        	return false;
	        }
        } //else if (DeliveryPolicy.CHECK_ERR == whatToCheck) //a single failed delivery is enough to consider the whole policy as failed
            throw new DeliveryPolicyFailedException(); 
        
	}
}