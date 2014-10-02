package at.ac.tuwien.dsg.smartcom.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.PushTask;
import at.ac.tuwien.dsg.smartcom.adapter.util.TaskScheduler;
import at.ac.tuwien.dsg.smartcom.adapters.rest.JsonMessageDTO;
import at.ac.tuwien.dsg.smartcom.broker.InputPublisher;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

public class RESTInputAdapterTest {

    private Client client;
    private ExecutorService executor;
    private RESTInputAdapter adapter;
    private Publisher publisher;

    private int port;

    @Before
    public void setUp() throws Exception {
        publisher = new Publisher();

        port = FreePortProviderUtil.getFreePort();

        adapter = new RESTInputAdapter(port, "test");
        adapter.setInputPublisher(publisher);
        adapter.setScheduler(new Scheduler());
        adapter.init();

        client = ClientBuilder.newBuilder().register(JacksonFeature.class).register(new ApplicationBinder(publisher)).build();
        //client.register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true));
        executor = Executors.newFixedThreadPool(5);
    }

    @After
    public void tearDown() throws Exception {
        adapter.cleanUp();
        executor.shutdown();
    }

    @Test(timeout = 20000l)
    public void testRESTInputAdapter() throws Exception {
        final WebTarget target = client.target("http://localhost:"+port+"/test");

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

        final CountDownLatch latch = new CountDownLatch(20);

        publisher.setLatch(latch);

        for (int i = 0; i < 20; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(new JsonMessageDTO(message)), Response.class);
                    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
                }
            });
        }

        latch.await();

    }

    private class Publisher implements InputPublisher {

        private CountDownLatch latch;

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void publishInput(Message message) {
            latch.countDown();
        }
    }

    private class Scheduler implements TaskScheduler {

        @Override
        public PushTask schedule(PushTask task) {
            return null;
        }
    }

    private class ApplicationBinder extends AbstractBinder {

        private final Publisher publisher;

        private ApplicationBinder(Publisher publisher) {
            this.publisher = publisher;
        }

        @Override
        protected void configure() {
            bind(publisher).to(InputPublisher.class);
        }
    }
}