package at.ac.tuwien.dsg.smartcom.rest;

import at.ac.tuwien.dsg.smartcom.Communication;
import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.exception.InvalidRuleException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;
import at.ac.tuwien.dsg.smartcom.rest.model.MessageDTO;
import at.ac.tuwien.dsg.smartcom.rest.model.NotificationDTO;
import at.ac.tuwien.dsg.smartcom.rest.model.RoutingRuleDTO;
import at.ac.tuwien.dsg.smartcom.statistic.Statistic;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import jersey.repackaged.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
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

    @Inject
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

    public CommunicationRESTImpl() {
        this(8080, "");
    }

    public CommunicationRESTImpl(int port, String serverURIPostfix) {
        this.serverURI = URI.create("http://localhost:" + port + "/" + serverURIPostfix);
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
    public void registerNotificationCallback(NotificationDTO callback) {
        if (!callback.getUrl().startsWith("http://")) {
            callback.setUrl("http://"+callback.getUrl());
        }

        communication.registerNotificationCallback(new NotificationRESTCallback(callback.getUrl()));
    }

    private class NotificationRESTCallback implements NotificationCallback {

        private final Client client;
        private final String url;

        private NotificationRESTCallback(String url) {
            this.url = url;
            this.client = ClientBuilder.newBuilder()
                    .register(JacksonFeature.class)
                    .property(ClientProperties.CONNECT_TIMEOUT, 1000)
                    .property(ClientProperties.READ_TIMEOUT,    1000)
                    .build();
        }

        @Override
        public void notify(final Message message) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        WebTarget target = client.target(url);

                        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(new MessageDTO(message)), Response.class);

                        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                            log.error("Could not send message {} to notification callback \nResponse: {}", message, response);
                        }
                    } catch (Exception ignored) {
                        log.debug("Could not notify rest callback", ignored);
                    }
                }
            });
        }
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
