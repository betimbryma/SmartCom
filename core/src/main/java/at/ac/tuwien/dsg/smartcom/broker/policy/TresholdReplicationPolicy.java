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
 * Defines the replication policy based on a treshold. If the
 * received messages per active handler exceeds a certain treshold,
 * the policy indicates an upscaling, if it is below a certain treshold,
 * it indicates a downscaling.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class TresholdReplicationPolicy implements ReplicationPolicy {
    private static final int REPLICATION_UP = 200;
    private static final int REPLICATION_DOWN = 10;
    private static final int MAX_UPSCALE_PER_TURN = 10;

    @Override
    public ReplicationPolicyResult determineReplicationPolicy(int messagesReceived, int handlers, int messagesPending, int messagesHandled) {
        int entriesPerListener = messagesReceived / handlers;

        if (entriesPerListener > REPLICATION_UP) {
            int times = Math.min(messagesReceived / REPLICATION_UP, MAX_UPSCALE_PER_TURN);
            return new ReplicationPolicyResult(ReplicationType.UPSCALE, times);
        } else if (entriesPerListener < REPLICATION_DOWN && handlers > 2) {
            return new ReplicationPolicyResult(ReplicationType.DOWNSCALE, 1);
        } else {
            return new ReplicationPolicyResult(ReplicationType.NOSCALE, 0);
        }

    }
}
