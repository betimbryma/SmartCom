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
package at.ac.tuwien.dsg.smartcom.manager.auth.dao;

import at.ac.tuwien.dsg.smartcom.model.Identifier;

import java.util.Date;

/**
 * DAO that can be used to store and assert the validity of a session of a given user.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface AuthenticationSessionDAO {

    /**
     * Persist a session and save it in the database
     * @param peerId id of the peer which owns the session
     * @param token security token that will be used by the session
     * @param expires expiration date of the session
     */
    public void persistSession(Identifier peerId, String token, Date expires);

    /**
     * Checks if the given session is still valid for a given peer. It returns false if either
     * the session is not valid or expired. In both cases the session should be renewed.
     *
     * @param peerId id of the peer which owns the session
     * @param token security token that is used by the session
     * @return true if the session is valid for a user and false otherwise
     */
    public boolean isValidSession(Identifier peerId, String token);
}
