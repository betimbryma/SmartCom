package at.ac.tuwien.dsg.smartcom.utils;

public class TimeBasedUUID {
	
	
	/**
     * Gets a new time uuid, fulfilling version 1 UUID requirements, and statistically guaranteeing 
     * that on the same machine up to 10,000 threads executing at the same time should get unique IDs.
     * 
     * @return the time uuid
     */
    public static java.util.UUID getUUID()
    {  	
    	return java.util.UUID.fromString(new com.eaio.uuid.UUID().toString()); 
    	//http://johannburkard.de/software/uuid/
    	//https://wiki.apache.org/cassandra/TimeBaseUUIDNotes
    }
    
    public static String getUUIDAsString()
    {  	
    	return (new com.eaio.uuid.UUID()).toString(); 
    }
    
    /**
     * Returns a long representing the timestamp used for generation the UUID passed as input parameter
     * 
     * @param string representation of uuid from which to extract the timestamp. Note that UUID must be V1 UUID with upper 64b representing the timestamp.
     * @return
     */
    public static long getTimeFromUUID(String uuid){
    	com.eaio.uuid.UUID eaio = new com.eaio.uuid.UUID(uuid);
    	return eaio.getTime();
    }
}
