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
