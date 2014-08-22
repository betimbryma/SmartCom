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
package at.ac.tuwien.dsg.smartcom.model;

/**
* @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
* @version 1.0
*/
public enum DeliveryPolicy {;

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
}
