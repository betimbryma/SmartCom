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
package at.ac.tuwien.dsg.smartcom.callback;

import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;

/**
 * The Peer Manager Callback (PMCallback) will be used to ask the Peer Manager for different
 * addressing possibilities for a specific peer (e.g., phone number, email address, skype username)
 * and an adapter will be selected based on the returned address. There might be multiple ways
 * on communication with a single peers, therefore the method returns a collection of addresses.
 *
 * This API is also used to check the user credentials with the PM. This allows the Middleware
 * to issue authentication tokens that will get embedded within the messages exchanged subsequently
 * between peer applications and output/input adapters and enable message authentication, and
 * possibly encryption.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface PeerAuthenticationCallback {

    /**
     * Authenticates a peer, i.e. checks if the provided credentials match the peers credentials in the system.
     *
     * @param peerId id of the peer
     * @param password password of the peer
     * @return Returns true if the credentials are valid, false otherwise
     * @throws PeerAuthenticationException if an authentication error occurs.
     */
    public boolean authenticate(Identifier peerId, String password) throws PeerAuthenticationException;
}
