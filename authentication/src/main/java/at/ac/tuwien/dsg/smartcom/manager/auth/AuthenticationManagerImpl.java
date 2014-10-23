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
package at.ac.tuwien.dsg.smartcom.manager.auth;

import at.ac.tuwien.dsg.smartcom.manager.AuthenticationManager;
import at.ac.tuwien.dsg.smartcom.manager.auth.dao.AuthenticationSessionDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an implementation for the AuthenticationManager interface that can be used
 * to authenticate a peer and check if his security token is still valid.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class AuthenticationManagerImpl implements AuthenticationManager {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationManagerImpl.class);

    @Inject
    private AuthenticationSessionDAO dao; //used to check if a token is valid

    @Override
    public boolean authenticate(Identifier peerId, String securityToken) {
        log.debug("Authenticating peer {} and security token {}", peerId, securityToken);
        return dao.isValidSession(peerId, securityToken);
    }
}
