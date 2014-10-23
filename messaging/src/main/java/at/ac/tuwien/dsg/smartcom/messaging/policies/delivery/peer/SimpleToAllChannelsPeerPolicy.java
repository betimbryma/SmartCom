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