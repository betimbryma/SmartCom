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
package at.ac.tuwien.dsg.smartcom.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.util.TaskScheduler;
import at.ac.tuwien.dsg.smartcom.adapters.rest.JsonMessageDTO;
import at.ac.tuwien.dsg.smartcom.adapters.rest.ObjectMapperProvider;
import at.ac.tuwien.dsg.smartcom.broker.InputPublisher;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Path("/")
@Singleton
public class RESTInputAdapter extends InputPushAdapter {
    private static final Logger log = LoggerFactory.getLogger(RESTOutputAdapter.class);

    private HttpServer server;
    private final URI serverURI;

    /**
     * @deprecated only used by frameworks
     */
    @Inject
    public RESTInputAdapter(InputPublisher publisher, TaskScheduler scheduler) {
        this(8080, "dummy");
        setInputPublisher(publisher);
        setScheduler(scheduler);
    }

    public RESTInputAdapter(int port, String serverURIPostfix) {
        this.serverURI = URI.create("http://localhost:"+port+"/"+serverURIPostfix);
    }

    @Override
    protected void cleanUp() {
        server.shutdown();
    }

    @Override
    public void init() {
        server = GrizzlyHttpServerFactory.createHttpServer(serverURI, new RESTApplication());
        try {
            server.start();
        } catch (IOException e) {
            log.error("Could not initialize RESTInputAdapter", e);
        }
    }

    private class RESTApplication extends ResourceConfig {
        private RESTApplication() {
            register(RESTInputAdapter.class);
            register(ObjectMapperProvider.class);
            register(JacksonFeature.class);
            register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(inputPublisher).to(InputPublisher.class);
                    bind(scheduler).to(TaskScheduler.class);
                }
            });
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response message(JsonMessageDTO message) {
        if (message == null) {
            throw new WebApplicationException();
        }

        publishMessage(message.createMessage());

        return Response.status(Response.Status.OK).build();
    }
}
