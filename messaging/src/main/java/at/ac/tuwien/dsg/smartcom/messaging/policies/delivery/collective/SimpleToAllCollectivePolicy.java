package at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.collective;

import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.AbstractDeliveryPolicy;

public class SimpleToAllCollectivePolicy extends AbstractDeliveryPolicy {
	
	
	public SimpleToAllCollectivePolicy(){
		super("SimpleToAllCollectivePolicy");
	}
	
	public boolean check(int whatToCheck){
		return true;
	}
}
