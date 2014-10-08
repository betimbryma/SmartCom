package at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.collective;

import at.ac.tuwien.dsg.smartcom.exception.DeliveryPolicyFailedException;
import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.AbstractDeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;

public class SimpleToAnyCollectivePolicy extends AbstractDeliveryPolicy {
	
	private int allowedFailuresRemaining;
	private boolean succeeded = false;
	
	public SimpleToAnyCollectivePolicy(int collectiveMembers){
		super("SimpleToAnyCollectivePolicy");
		allowedFailuresRemaining = collectiveMembers - 1;
	}
	
	 
	public boolean check(int whatToCheck) throws DeliveryPolicyFailedException{
		if (whatToCheck == DeliveryPolicy.CHECK_ERR){
			if (allowedFailuresRemaining-- > 0) {
				return false; //still valid, but not yet succeeded
			}else{ //conclusively failed
				throw new at.ac.tuwien.dsg.smartcom.exception.DeliveryPolicyFailedException();
			}
		}else if (whatToCheck == DeliveryPolicy.CHECK_ACK) {
			//the only remaining option is that at least once 
			if (!succeeded){ //if succeeding for the first time return true. Otherwise return false, so that the purging attempts does not repeat
				succeeded = true;
				return true;
			}
		}
		return false;
	}
}
