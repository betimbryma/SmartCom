package at.ac.tuwien.dsg.smartcom.messaging.policies.privacy.peer;

import at.ac.tuwien.dsg.smartcom.model.PrivacyPolicy;

public abstract class AbstractPrivacyPolicy implements PrivacyPolicy{
	
public final String name;
	
	public AbstractPrivacyPolicy(String name){
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractPrivacyPolicy other = (AbstractPrivacyPolicy) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	
}
