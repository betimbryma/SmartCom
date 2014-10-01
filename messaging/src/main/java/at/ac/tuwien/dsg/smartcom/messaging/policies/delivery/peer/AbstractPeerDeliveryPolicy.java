package at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.peer;

import at.ac.tuwien.dsg.smartcom.exception.DeliveryPolicyFailedException;
import at.ac.tuwien.dsg.smartcom.messaging.policies.delivery.AbstractDeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;

public abstract class AbstractPeerDeliveryPolicy extends AbstractDeliveryPolicy{
		
		public final String name;
		public final PeerInfo peer;
		
		public AbstractPeerDeliveryPolicy(String name, PeerInfo peer){
			super(name);
			this.name = name;
			this.peer = peer;
			
		}

		

		
		
}
