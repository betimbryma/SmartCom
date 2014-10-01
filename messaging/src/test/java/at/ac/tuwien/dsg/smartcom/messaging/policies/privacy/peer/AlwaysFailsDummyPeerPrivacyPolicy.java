package at.ac.tuwien.dsg.smartcom.messaging.policies.privacy.peer;

import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PrivacyPolicy;

public class AlwaysFailsDummyPeerPrivacyPolicy extends AbstractPrivacyPolicy {
	
	
	public AlwaysFailsDummyPeerPrivacyPolicy(){
		super("AlwaysFailsDummyPeerPrivacyPolicy");
	}
	
	@Override
	public boolean condition(Message msg){
		return false;
	}
	
}
