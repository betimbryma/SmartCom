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
package at.ac.tuwien.dsg.smartcom.manager.messaging.policies.delivery.peer;

import at.ac.tuwien.dsg.smartcom.exception.DeliveryPolicyFailedException;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;

import java.util.concurrent.atomic.AtomicInteger;


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
