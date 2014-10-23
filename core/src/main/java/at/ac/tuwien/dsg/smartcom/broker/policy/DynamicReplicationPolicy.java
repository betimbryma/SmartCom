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
 * Policy that decides based on messages received, pending messages and messages handled.
 * It is more dynamic
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class DynamicReplicationPolicy implements ReplicationPolicy {

    @Override
    public ReplicationPolicyResult determineReplicationPolicy(int messagesReceived, int handlers, int messagesPending, int messagesHandled) {

        int remainingMessages = messagesReceived + messagesPending;

        if ((remainingMessages * 0.99f) > messagesHandled) {
            int deviationPerHandler = remainingMessages/handlers;
            int handledPerHandler = messagesHandled/handlers;

            return new ReplicationPolicyResult(ReplicationType.UPSCALE, deviationPerHandler/handledPerHandler);
        } else if ((remainingMessages * 1.01f) < messagesHandled || messagesPending < 100) {
            int deviation = messagesHandled - remainingMessages;
            int handledPerHandler = Math.min(messagesHandled/handlers, 1);

            return new ReplicationPolicyResult(ReplicationType.DOWNSCALE, (deviation/handledPerHandler) - 1);
            //TODO please check if there is an error in the code above
        } else {
            return new ReplicationPolicyResult(ReplicationType.NOSCALE, 0);
        }
    }
}
