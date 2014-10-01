package at.ac.tuwien.dsg.smartcom.adapters.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Path("message")
public class RESTResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response message(JsonMessageDTO message) {
        if (message == null) {
            throw new WebApplicationException();
        }
        TestSynchronizer.countDown();
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String message() {
       return "Alright!";
    }
}
