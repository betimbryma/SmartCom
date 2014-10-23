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
package at.ac.tuwien.dsg.smartcom.broker.policy;

/**
 * Defines the policy on when to scale up, down or do not scale at all.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface ReplicationPolicy {

    /**
     * Defines the replication policy based on the parameters passed to the method. Decides whether to scale up, down or do
     * not scale at all.
     *
     * @param messagesReceived number of messages that have been received since the last call
     * @param handlers number of handlers that are already handling the messages
     * @param messagesPending messages that are currently waiting in the queue to be processed
     * @param messagesHandled messages handled since the last call
     * @return the decision whether to scale up, down or not at all as well as the number of resources that should be added/removed
     */
    ReplicationPolicyResult determineReplicationPolicy(int messagesReceived, int handlers, int messagesPending, int messagesHandled);
}
