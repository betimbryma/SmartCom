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

import at.ac.tuwien.dsg.smartcom.Communication;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.exception.InvalidRuleException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;
import at.ac.tuwien.dsg.smartcom.rest.NotificationCallback.NotificationRESTCallback;
import at.ac.tuwien.dsg.smartcom.rest.model.MessageDTO;
import at.ac.tuwien.dsg.smartcom.rest.model.NotificationDTO;
import at.ac.tuwien.dsg.smartcom.rest.model.RoutingRuleDTO;
import at.ac.tuwien.dsg.smartcom.statistic.Statistic;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import jersey.repackaged.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Path("SmartCom")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class CommunicationRESTImpl {
    private static final Logger log = LoggerFactory.getLogger(CommunicationRESTImpl.class);

    private HttpServer server;
    private final URI serverURI;

    private ExecutorService executorService;

    @Inject
    private Communication communication;

    @Inject
    private StatisticBean statistic;

    public CommunicationRESTImpl(Communication communication, StatisticBean statistic) {
        this();
        this.communication = communication;
        this.statistic = statistic;
    }

    public CommunicationRESTImpl(int port, String serverURIPostfix, Communication communication, StatisticBean statistic) {
        this(port, serverURIPostfix);
        this.communication = communication;
        this.statistic = statistic;
        //WebClie
    }

    public CommunicationRESTImpl() {
        this(8080, "");
    }

    public CommunicationRESTImpl(int port, String serverURIPostfix) {
        this.serverURI = URI.create("http://0.0.0.0:" + port + "/" + serverURIPostfix);
    }

    public void cleanUp() {
        server.shutdown();

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Could not await termination of executor. forcing shutdown", e);
            executorService.shutdownNow();
        }
    }

    public void init() {
        executorService = Executors.newFixedThreadPool(10, new ThreadFactoryBuilder().setNameFormat("REST-notification-thread-%d").build());
        server = GrizzlyHttpServerFactory.createHttpServer(serverURI, new RESTApplication());
        try {
            server.start();
        } catch (IOException e) {
            log.error("Could not initialize CommunicationRESTImpl", e);
        }
    }

    @POST
    @Path("message")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String send(MessageDTO message) throws CommunicationException {
        if (message == null) {
            throw new WebApplicationException();
        }

        Identifier id = communication.send(message.create());

        if (id == null) {
            return null;
        }
        return id.getId();
    }

    @POST
    @Path("route")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addRouting(RoutingRuleDTO rule) throws InvalidRuleException {
        if (rule == null) {
            throw new WebApplicationException();
        }

        communication.addRouting(rule.create());

        return null;
    }

    @DELETE
    @Path("route/{routeId}")
    public RoutingRuleDTO removeRouting(@PathParam("routeId") String routeId) {
        if (routeId == null) {
            throw new WebApplicationException();
        }

        RoutingRule routingRule = communication.removeRouting(Identifier.routing(routeId));

        if (routingRule != null) {
            return new RoutingRuleDTO(routingRule);
        }

        return null;
    }

    @GET
    @Path("statistic")
    @Produces(MediaType.APPLICATION_JSON)
    public Statistic statistic() {
        return statistic.getStatistic();
    }

    @POST
    @Path("notification")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerNotificationCallback(NotificationDTO callback) {
        if (callback != null && !callback.getUrl().startsWith("http://")) {
            callback.setUrl("http://"+callback.getUrl());
        }

        communication.registerNotificationCallback(new NotificationRESTCallback(callback.getUrl()));
        return Response.ok().build();
    }

    @POST
    @Path("adapter/push/add")
    @Produces(MediaType.APPLICATION_JSON)
    public Identifier addPushAdapter(InputPushAdapter adapter) {
        if(adapter == null)
            throw new WebApplicationException();
        return communication.addPushAdapter(adapter);
    }

    @POST
    @Path("adapter/pull/add/{interval}")
    @Produces(MediaType.APPLICATION_JSON)
    public Identifier addPullAdapter(InputPullAdapter adapter, @PathParam("interval") long interval){
        if(adapter == null)
            throw new WebApplicationException();
        return communication.addPullAdapter(adapter, interval);
    }

    @DELETE
    @Path("adapter/{adapterId}")
    public Response removeInputAdapter(@PathParam("adapterId") String adapterId) {
        if(adapterId == null)
            throw new WebApplicationException();

        Identifier identifier = Identifier.adapter(adapterId);

        return Response.ok().build();
    }

    @POST
    @Path("adapter/output/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Identifier registerOutputAdapter(Class<? extends OutputAdapter> outputAdapter) throws CommunicationException {
        if(outputAdapter == null)
            throw new WebApplicationException();
        return null;//communication.registerOutputAdapter(outputAdapter);
    }

    private class RESTApplication extends ResourceConfig {
        private RESTApplication() {
            register(CommunicationRESTImpl.class);
            register(ObjectMapperProvider.class);
            register(JacksonFeature.class);
//            register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true));
            register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(communication).to(Communication.class);
                    bind(executorService).to(ExecutorService.class);
                    bind(statistic).to(StatisticBean.class);
                }
            });
        }
    }
}
