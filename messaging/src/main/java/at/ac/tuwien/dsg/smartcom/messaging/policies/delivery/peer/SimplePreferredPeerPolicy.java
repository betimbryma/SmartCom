package at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.peer;

import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.AbstractDeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;

public class SimplePreferredPeerPolicy extends AbstractPeerDeliveryPolicy {
	
	
	public SimplePreferredPeerPolicy(){
		super("SimplePreferredPeerPolicy", null);
	}
	public boolean check(int whatToCheck){
		if (whatToCheck == DeliveryPolicy.CHECK_ACK){
			return true; //we imply that this policy initially restricted the choice of adapters to 1, meaning this must be response from that one. 
		}else{
			return false; 
		}
	}
}