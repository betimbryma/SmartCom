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
package at.ac.tuwien.dsg.smartcom.integration;

import at.ac.tuwien.dsg.smartcom.callback.CollectiveInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerAuthenticationCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.model.CollectiveInfo;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;
import at.ac.tuwien.dsg.smartcom.utils.PropertiesLoader;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class SmartSocRESTPeerManager implements PeerAuthenticationCallback, PeerInfoCallback, CollectiveInfoCallback {
    private static final Logger log = LoggerFactory.getLogger(SmartSocRESTPeerManager.class);

    private final Client client;
    private final String collectiveURL;
    private final String peerURL;
    private final String authenticationURL;

    private final long userId;

    public SmartSocRESTPeerManager(long userId) {
        this.userId = userId;
        this.client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
                .property(ClientProperties.CONNECT_TIMEOUT, 1000)
                .property(ClientProperties.READ_TIMEOUT,    1000)
                .build();

        collectiveURL = PropertiesLoader.getProperty("pm.properties", "collectiveURL");
        peerURL = PropertiesLoader.getProperty("pm.properties", "peerURL");
        authenticationURL = PropertiesLoader.getProperty("pm.properties", "authenticationURL");
    }

    @Override
    public CollectiveInfo getCollectiveInfo(Identifier collective) throws NoSuchCollectiveException {
        try {
            WebTarget target = client.target(collectiveURL + "/" + collective.getId());
            target.queryParam("userId", userId);
            target.queryParam("attributeDefinitionIds", "NULL");

            ClientResponse response = target.request(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            String content = response.readEntity(String.class);
            log.trace(content);

            return JSONConverter.getCollectiveInfo(collective, content);
        } catch (NoSuchCollectiveException e) {
            throw e;
        } catch (Exception e) {
            log.error("Could not get collective info for collective {}", collective, e);
            throw new NoSuchCollectiveException();
        }
    }

    @Override
    public boolean authenticate(Identifier peerId, String password) throws PeerAuthenticationException {

        try {
            WebTarget target = client.target(authenticationURL + "/" + peerId.getId());
            target.queryParam("password", password);
            target.queryParam("userId", userId);

            Response response = target.request(MediaType.APPLICATION_JSON).get();
            String content = response.readEntity(String.class);
            log.trace(content);

            return Boolean.valueOf(content);
        } catch (Exception e) {
            log.error("Could not authentication peer {}", peerId, e);
            throw new PeerAuthenticationException();
        }
    }

    @Override
    public PeerInfo getPeerInfo(Identifier id) throws NoSuchPeerException {

        try {
            WebTarget target = client.target(peerURL+ "/" + id.getId());
            target.queryParam("userId", userId);
            target.queryParam("attributeDefinitionIds", "NULL");

            Response response = target.request(MediaType.APPLICATION_JSON).get();
            String content = response.readEntity(String.class);
            log.trace(content);

            return JSONConverter.getPeerInfo(id, content);
        } catch (NoSuchPeerException e) {
            throw e;
        } catch (Exception e) {
            log.error("Could not get peer info for peer {}", id, e);
            throw new NoSuchPeerException(id);
        }
    }
}
