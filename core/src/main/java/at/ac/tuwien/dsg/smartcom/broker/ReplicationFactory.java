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
package at.ac.tuwien.dsg.smartcom.broker;

/**
 * Factory that creates a replica of a specific listener. This class is used
 * by the ReplicatingMessageListener to externalise the creation of a
 * replica instance.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface ReplicationFactory {

    /**
     * Create the replica of a message listener. Does not necessarily have
     * to create a new instance. E.g., stateless instances could be reused if
     * this does not prohibit a performance gain (e.g., by additional synchronisation).
     * @return (new) instance of a message listener (a so called replica)
     */
    public MessageListener createReplication();
}
