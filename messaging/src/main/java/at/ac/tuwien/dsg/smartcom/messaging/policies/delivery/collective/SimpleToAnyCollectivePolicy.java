package at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.collective;

import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.AbstractDeliveryPolicy;

public class SimpleToAnyCollectivePolicy extends AbstractDeliveryPolicy {
	
	
	public SimpleToAnyCollectivePolicy(){
		super("SimpleToAnyCollectivePolicy");
	}
	
	
	public boolean check(int whatToCheck){
		return true;
	}
}
