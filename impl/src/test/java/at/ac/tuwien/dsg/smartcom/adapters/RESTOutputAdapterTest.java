package at.ac.tuwien.dsg.smartcom.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.adapters.rest.ObjectMapperProvider;
import at.ac.tuwien.dsg.smartcom.adapters.rest.RESTResource;
import at.ac.tuwien.dsg.smartcom.adapters.rest.TestSynchronizer;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RESTOutputAdapterTest {

    private HttpServer server;
    private ExecutorService executor;

    @Before
    public void setUp() throws Exception {
        final ResourceConfig application = new ResourceConfig(
                RESTResource.class,
                ObjectMapperProvider.class,
                JacksonFeature.class
        );

        server = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:8080/"), application);
        server.start();

        executor = Executors.newFixedThreadPool(5);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
        executor.shutdown();
    }

    @Test(timeout = 10000l)
    public void testRESTAdapter() throws Exception {
        TestSynchronizer.initSynchronizer(20);

        final Message message = new Message.MessageBuilder()
                .setId(Identifier.message("testId"))
                .setContent("testContent")
                .setType("testType")
                .setSubtype("testSubType")
                .setSenderId(Identifier.peer("sender"))
                .setReceiverId(Identifier.peer("receiver"))
                .setConversationId("conversationId")
                .setTtl(3)
                .setLanguage("testLanguage")
                .setSecurityToken("securityToken")
                .create();

        final RESTOutputAdapter adapter = new RESTOutputAdapter();
        final List<Serializable> list = new ArrayList<>();
        list.add("http://localhost:8080/message/");

        for (int i = 0; i < 20; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        adapter.push(message, new PeerAddress(Identifier.peer("peer"), Identifier.adapter("adapter"), list));
                    } catch (AdapterException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        TestSynchronizer.await();
    }
}