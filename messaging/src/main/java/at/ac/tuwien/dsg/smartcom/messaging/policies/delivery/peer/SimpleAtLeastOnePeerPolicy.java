package at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.peer;

import java.util.concurrent.atomic.AtomicInteger;

import at.ac.tuwien.dsg.smartcom.exception.DeliveryPolicyFailedException;
import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.AbstractDeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;


	public class SimpleAtLeastOnePeerPolicy extends AbstractPeerDeliveryPolicy {
		
		private java.util.concurrent.atomic.AtomicInteger numOfFailedDeliveries;
		private final int numberOfChannels;
		
		public SimpleAtLeastOnePeerPolicy(PeerInfo peerInfo){
			super("SimpleAtLeastOnePeerPolicy", peerInfo);
			numberOfChannels = peerInfo.getAddresses().size(); //get channels
			numOfFailedDeliveries = new AtomicInteger(0);
		}
		
		public boolean check(int whatToCheck) throws DeliveryPolicyFailedException {
			
			if (whatToCheck == DeliveryPolicy.CHECK_ACK){
				return true; //even 1 ACK suffices to consider the entire policy as successful. 
			} else {
		        if (numOfFailedDeliveries.incrementAndGet() < numberOfChannels){
		        	return false; //there is still a chance to succeed later on
		        }else{
		        	throw new DeliveryPolicyFailedException();
		        }
			}
		}
		
	}
