package at.ac.tuwien.dsg.smartcom.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.adapters.rest.JsonMessageDTO;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Adapter(name = "REST", stateful = false)
public class RESTOutputAdapter implements OutputAdapter {
    private static final Logger log = LoggerFactory.getLogger(RESTOutputAdapter.class);

    private final Client client;

    public RESTOutputAdapter() {
        this.client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
//        client.register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true)); enables this to have additional logging information
    }

    @Override
    public void push(Message message, PeerAddress address) {
        String url = (String) address.getContactParameters().get(0);

        WebTarget target = client.target(url);

        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(new JsonMessageDTO(message)), Response.class);

        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            log.error("Could not send message {} to peer address {}\nResponse: {}", message, address, response);
        }
    }
}
