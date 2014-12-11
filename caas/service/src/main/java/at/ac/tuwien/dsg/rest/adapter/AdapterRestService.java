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
package at.ac.tuwien.dsg.rest.adapter;

import at.ac.tuwien.dsg.rest.adapter.resources.AdapterResource;
import at.ac.tuwien.dsg.rest.adapter.resources.DropboxAdapterResource;
import at.ac.tuwien.dsg.rest.adapter.resources.EmailAdapterResource;
import at.ac.tuwien.dsg.rest.adapter.resources.RESTAdapterResource;
import at.ac.tuwien.dsg.smartcom.Communication;
import at.ac.tuwien.dsg.smartcom.adapter.InputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapter;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.rest.ObjectMapperProvider;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class AdapterRestService {
    private static final Logger log = LoggerFactory.getLogger(AdapterRestService.class);

    private final Communication communication;

    private HttpServer server;
    private URI serverURI;

    public AdapterRestService(int port, String serverURIPostfix, Communication communication) {
        this.communication = communication;
        this.serverURI = URI.create("http://0.0.0.0:" + port + "/" + serverURIPostfix);
    }

    public AdapterRestService(Communication communication) {
        this.communication = communication;
    }

    public void init() {
        ResourceConfig config = new ResourceConfig();
        registerResourceConfig(this, config);

        server = GrizzlyHttpServerFactory.createHttpServer(serverURI, config);
        try {
            server.start();
        } catch (IOException e) {
            log.error("Could not initialize AdapterRest", e);
        }
    }

    public void cleanUp() {
        if (server != null) {
            server.shutdown();
        }
    }

    public Identifier addPushAdapter(InputPushAdapter adapter) {
        return communication.addPushAdapter(adapter);
    }

    public Identifier addPullAdapter(InputPullAdapter adapter, long interval) {
        return communication.addPullAdapter(adapter, interval);
    }

    public Identifier addPullAdapter(InputPullAdapter adapter, long interval, boolean deleteIfSuccessful) {
        return communication.addPullAdapter(adapter, interval, deleteIfSuccessful);
    }

    public InputAdapter removeInputAdapter(Identifier adapterId) {
        return communication.removeInputAdapter(adapterId);
    }

    public static void registerResourceConfig(final AdapterRestService restService, ResourceConfig config) {
        registerWebResources(config);

        config.register(MultiPartFeature.class);
        config.register(ObjectMapperProvider.class);
        config.register(JacksonFeature.class);
//          config.register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true));
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindServices(restService, this);
            }
        });
    }

    public static void registerWebResources(ResourceConfig config) {
        config.register(AdapterResource.class);
        config.register(EmailAdapterResource.class);
        config.register(RESTAdapterResource.class);
        config.register(DropboxAdapterResource.class);
    }

    public List<Class<?>> getWebResources() {
        return Arrays.asList(AdapterResource.class,
                EmailAdapterResource.class,
                RESTAdapterResource.class,
                DropboxAdapterResource.class,
                MultiPartFeature.class,
                ObjectMapperProvider.class,
                JacksonFeature.class);
    }

    public void bindWebBindings(AbstractBinder binder) {
        binder.bind(this).to(AdapterRestService.class);
    }

    public static void bindServices(AdapterRestService restService, AbstractBinder binder) {
        binder.bind(restService).to(AdapterRestService.class);
    }
}
