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

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.callback.PeerAuthenticationCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.manager.auth.dao.AuthenticationSessionDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.utils.PicoHelper;
import org.junit.Before;

import java.util.Date;

public class AuthenticationRequestHandlerTest extends AuthenticationRequestHandlerTestClass {

    @Override
    @Before
    public void setUp() throws Exception {
        pico = new PicoHelper();
        pico.addComponent(new SimpleAuthenticationSessionDAO());
        pico.addComponent(new SimplePeerAuthenticationCallback());
        pico.addComponent(new SimpleMessageBroker());
        pico.addComponent(AuthenticationRequestHandler.class);

        super.setUp();
    }

    private class SimpleAuthenticationSessionDAO implements AuthenticationSessionDAO {

        @Override
        public void persistSession(Identifier peerId, String token, Date expires) {

        }

        @Override
        public boolean isValidSession(Identifier peerId, String token) {
            return true;
        }
    }

    private class SimplePeerAuthenticationCallback implements PeerAuthenticationCallback {

        @Override
        public boolean authenticate(Identifier peerId, String password) throws PeerAuthenticationException {
            if ("true".equals(password)) {
                return true;
            }
            if ("false".equals(password)) {
                return false;
            }
            throw new PeerAuthenticationException();
        }
    }
}