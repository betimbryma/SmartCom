package at.ac.tuwien.dsg.smartcom.messaging.policies.delivery;

import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;

public abstract class AbstractDeliveryPolicy implements DeliveryPolicy{
	
	public final String name;
	
	public AbstractDeliveryPolicy(String name){
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
		AbstractDeliveryPolicy other = (AbstractDeliveryPolicy) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
