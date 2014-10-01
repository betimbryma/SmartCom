package at.ac.tuwien.dsg.smartcom.model;


import at.ac.tuwien.dsg.smartcom.exception.DeliveryPolicyFailedException;

public interface DeliveryPolicy {
    public static enum Collective {
        TO_ALL_MEMBERS, TO_ANY;
        
        public interface Policy{
        	
        }
    }
    
    public static enum Peer {
        TO_ALL_CHANNELS, AT_LEAST_ONE, PREFERRED;
    }
    
    public static enum Message {
        ACKNOWLEDGED, UNACKNOWLEDGED;
    }
    
    public boolean check(int whatToCheck) throws DeliveryPolicyFailedException;
    
    public static final int CHECK_ERR = 1;
    public static final int CHECK_ACK = 2;
    
}

/**
* @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
* @version 1.0
*/
/*public enum DeliveryPolicy {;

    public interface Policy {

        public int getValue();
    }

    public static enum Collective implements Policy {
        TO_ALL_MEMBERS(0), TO_ANY(1);

        private final int value;

        private Collective(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static enum Peer implements Policy {
        TO_ALL_CHANNELS(0), AT_LEAST_ONE(1), PREFERRED(2);

        private final int value;

        private Peer(int value) {
            this.value = value;
        }

        public int getValue() {
            return value+10;
        }
    }

    public static enum Message implements Policy {
        ACKNOWLEDGED(0), UNACKNOWLEDGED(1);

        private final int value;

        private Message(int value) {
            this.value = value;
        }

        public int getValue() {
            return value+20;
        }
    }
}*/
