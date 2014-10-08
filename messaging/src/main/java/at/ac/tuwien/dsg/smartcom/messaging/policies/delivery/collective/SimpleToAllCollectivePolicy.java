package at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.collective;

import at.ac.tuwien.dsg.smartcom.exception.DeliveryPolicyFailedException;
import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.AbstractDeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;

public class SimpleToAllCollectivePolicy extends AbstractDeliveryPolicy {
	
	private int requiredSuccessesRemaining;
	private boolean failed = false;
	
	public SimpleToAllCollectivePolicy(int collectiveMembers){
		super("SimpleToAllCollectivePolicy");
		requiredSuccessesRemaining = collectiveMembers;
	}
	
	//return true if policy conclusively succeeded, false if still valid but still not succeeded, Exception if conclusively failed.
	public boolean check(int whatToCheck) throws DeliveryPolicyFailedException{
		if (whatToCheck == DeliveryPolicy.CHECK_ACK){
			if (--requiredSuccessesRemaining == 0) {
				return true;
			}
		}else if (whatToCheck == DeliveryPolicy.CHECK_ERR){
			if (!failed){
				failed = true;
				throw new at.ac.tuwien.dsg.smartcom.exception.DeliveryPolicyFailedException();
			}
		}
		return false;
	}
}
