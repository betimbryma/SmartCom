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
