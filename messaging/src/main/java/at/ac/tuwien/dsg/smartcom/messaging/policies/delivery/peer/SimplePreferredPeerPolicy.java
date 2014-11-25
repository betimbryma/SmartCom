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