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
package at.ac.tuwien.dsg.smartcom.demo.peer;

import at.ac.tuwien.dsg.smartcom.adapters.dropbox.DropboxClientUtils;
import at.ac.tuwien.dsg.smartcom.adapters.rest.JsonMessageDTO;
import at.ac.tuwien.dsg.smartcom.adapters.rest.ObjectMapperProvider;
import com.dropbox.core.DbxException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class RESTToDropboxSoftwarePeer implements Peer {
    private static final Logger log = LoggerFactory.getLogger(RESTToDropboxSoftwarePeer.class);

    ExecutorService executor = Executors.newSingleThreadExecutor();

    private HttpServer server;
    private final URI serverURI;
    private final DropboxClientUtils client;
    private final String path;
    private final long timeout;

    private AtomicInteger jobCounter = new AtomicInteger(0);

    public RESTToDropboxSoftwarePeer(int port, String serverURIPostfix, long timeout, String accessToken, String path) {
        this.timeout = timeout;
        this.path = path;
        this.serverURI = URI.create("http://localhost:"+port+"/"+serverURIPostfix);
        this.client = new DropboxClientUtils(accessToken);
    }

    @Override
    public void initialize() {
        server = GrizzlyHttpServerFactory.createHttpServer(serverURI, new RESTApplication());
        try {
            server.start();
        } catch (IOException e) {
            log.error("Could not initialize RESTInputAdapter", e);
        }
    }

    @Override
    public void terminate() {
        server.shutdown();
        executor.shutdown();

        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    private class RESTApplication extends ResourceConfig {
        private RESTApplication() {
            register(RestHandler.class);
            register(ObjectMapperProvider.class);
            register(JacksonFeature.class);
            register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(RESTToDropboxSoftwarePeer.this).to(RESTToDropboxSoftwarePeer.class);
                }
            });
        }
    }

    @Path("/")
    @Singleton
    public static class RestHandler {

        @Inject RESTToDropboxSoftwarePeer peer;

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        public Response message(final JsonMessageDTO message) {
            if (message == null) {
                throw new WebApplicationException();
            }

            peer.submitTask(message);

            return Response.status(Response.Status.CREATED).build();
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String message() {
            return "success";
        }
    }

    protected void submitTask(final JsonMessageDTO message) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                registerTaskHandler(message);
            }
        });
    }

    private void registerTaskHandler(JsonMessageDTO message) {
        int jobCount = jobCounter.getAndIncrement();
        log.info("Starting job #"+jobCount);

        long localTimeout = timeout;
        try {
            localTimeout = Integer.valueOf(message.getContent());
        } catch (NumberFormatException ignored) {}

        JFrame frame = createWindow();

        synchronized(this) {
            try {
                wait(localTimeout);
            } catch (InterruptedException ignored) {}
        }

        frame.dispose();
        frame = null;

        File taskFile = null;
        try {
            taskFile = File.createTempFile("result_"+message.getConversation(), "result");
            FileWriter writer = new FileWriter(taskFile);

            writer.write("Finished with the execution!");
            writer.close();
        } catch (IOException e) {
            log.error("Could not create temporary file 'result_{}", message.getConversation());
            return;
        }

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(taskFile);
        } catch (FileNotFoundException e) {
            log.error("Could not create temporary file", e);
            return;
        }

        try {
            client.uploadFile(path, "result_"+message.getConversation()+".result", taskFile.length(), inputStream);
        } catch (IOException | DbxException e) {
            log.error("Could not upload file!", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
            log.info("Finished job #"+jobCount);
        }

    }

    private static JFrame createWindow() {
        //Create and set up the window.
        JFrame frame = new JFrame("Software Peer GUI");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        JLabel textLabel = new JLabel("Calculating...",SwingConstants.CENTER);
        textLabel.setPreferredSize(new Dimension(300, 100));
        frame.getContentPane().add(textLabel, BorderLayout.CENTER);
        //Display the window.
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
        return frame;
    }
}
