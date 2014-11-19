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
package at.ac.tuwien.dsg.peer.resources;

import at.ac.tuwien.dsg.peer.PeerMailboxService;
import at.ac.tuwien.dsg.smartcom.adapters.rest.JsonMessageDTO;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MailboxResource {

    @Inject
    private PeerMailboxService service;

    @GET
    @Path("/{id}")
    public JsonMessageDTO pullNextMessage(@PathParam("id") String id) {
        return service.pullMessage(id);
    }

    @GET
    @Path("/poll/{id}")
    public void poll(@PathParam("id") String id, @Suspended final AsyncResponse asyncResponse) {
        asyncResponse.setTimeout(30, TimeUnit.SECONDS);
        asyncResponse.setTimeoutHandler(new TimeoutHandler() {
            @Override
            public void handleTimeout(AsyncResponse asyncResponse) {
                asyncResponse.resume(Response.status(Response.Status.REQUEST_TIMEOUT).build());
            }
        });
        JsonMessageDTO messageDTO = pullNextMessage(id);
        if (messageDTO != null) {
            asyncResponse.resume(messageDTO);
        } else {
            service.registerAsynchResponse(asyncResponse, id);
        }
    }

    @GET
    @Path("/all/{id}")
    public List<JsonMessageDTO> getAllMessages(@PathParam("id") String id) {
        return service.getAllMessages(id);
    }

    @POST
    @Path("/{id}")
    public Response addMessage(@PathParam("id") String id, JsonMessageDTO message) {
        service.persistMessage(message, id);
        return Response.ok().build();
    }
}
