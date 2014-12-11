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
package at.ac.tuwien.dsg.peer;

import at.ac.tuwien.dsg.peer.dao.PeerMailboxDAO;
import at.ac.tuwien.dsg.peer.resources.MailboxResource;
import at.ac.tuwien.dsg.smartcom.adapters.rest.JsonMessageDTO;
import at.ac.tuwien.dsg.smartcom.rest.ObjectMapperProvider;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.AsyncResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerMailboxService {
    private static final Logger log = LoggerFactory.getLogger(PeerMailboxService.class);

    private HttpServer server;
    private final URI serverURI;

    @Inject
    private PeerMailboxDAO dao;

    private final Map<String, List<AsyncResponse>> asynchResponsesMap = new HashMap<>();

    public PeerMailboxService(int port, String serverURIPostfix) {
        this.serverURI = URI.create("http://0.0.0.0:" + port + "/" + serverURIPostfix);
    }

    public PeerMailboxService(int port, String serverURIPostfix, PeerMailboxDAO mailboxDAO) {
        this(port, serverURIPostfix);
        this.dao = mailboxDAO;
    }

    public void init() {
        server = GrizzlyHttpServerFactory.createHttpServer(serverURI, new RESTApplication());
        try {
            server.start();
        } catch (IOException e) {
            log.error("Could not initialize CommunicationRESTImpl", e);
        }
    }

    public void cleanUp() {
        if (server != null) {
            server.shutdown();
        }
    }

    public JsonMessageDTO pullMessage(String receiver) {
        return dao.pullMessageForReceiver(receiver);
    }

    public List<JsonMessageDTO> getAllMessages(String receiver) {
        return dao.getMessagesForReceiver(receiver);
    }

    public synchronized void persistMessage(JsonMessageDTO dto, String receiver) {
        List<AsyncResponse> asyncResponses = asynchResponsesMap.remove(receiver);
        if (asyncResponses == null) {
            dao.persistMessage(dto, receiver);
        } else {
            asyncResponses.get(0).resume(dto);
        }
    }

    public synchronized void registerAsynchResponse(AsyncResponse response, String receiver) {
        List<AsyncResponse> asyncResponses = asynchResponsesMap.get(receiver);
        if (asyncResponses == null) {
            asyncResponses = new ArrayList<>();
            asynchResponsesMap.put(receiver, asyncResponses);
        }

        asyncResponses.add(response);
    }

    private class RESTApplication extends ResourceConfig {
        private RESTApplication() {
            register(MailboxResource.class);

            register(MultiPartFeature.class);
            register(ObjectMapperProvider.class);
            register(JacksonFeature.class);
//            register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true));
            register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bindWebBindings(this);
                }
            });
        }
    }

    public List<Class<?>> getWebResources() {
        return Arrays.asList(MailboxResource.class,
                MultiPartFeature.class,
                ObjectMapperProvider.class,
                JacksonFeature.class);
    }

    public void bindWebBindings(AbstractBinder binder) {
        binder.bind(this).to(PeerMailboxService.class);
    }
}
