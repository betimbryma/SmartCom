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
package at.ac.tuwien.dsg.smartcom.rest;

import at.ac.tuwien.dsg.smartcom.exception.InvalidRuleException;
import at.ac.tuwien.dsg.smartcom.rest.model.MessageDTO;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

@Path("/")
public class RestTest {
    private static final Logger log = LoggerFactory.getLogger(RestTest.class);

    private HttpServer server;
    private URI serverURI;

    public RestTest(int port, String serverURIPostfix) {
        serverURI = URI.create("http://localhost:" + port + "/" + serverURIPostfix);
    }

    public RestTest() {
        this(9090, "test");
    }

    protected void startUp() {
        server = GrizzlyHttpServerFactory.createHttpServer(serverURI, new RESTApplication());
        try {
            server.start();
        } catch (IOException e) {
            log.error("Could not initialize CommunicationRESTImpl", e);
        }
    }

    protected void cleanUp() {
        server.shutdown();
    }

    private class RESTApplication extends ResourceConfig {
        private RESTApplication() {
            register(RestTest.class);
            register(ObjectMapperProvider.class);
//            register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true));
            register(JacksonFeature.class);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void notify(MessageDTO message) throws InvalidRuleException {
        if (message != null) {
            System.out.println("Received message: " + message);
        }
    }

    public static void main(String[] args) throws IOException {
        RestTest test = new RestTest();
        test.startUp();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("press enter to shutdown server:");
        reader.readLine();

        test.cleanUp();
    }
}
