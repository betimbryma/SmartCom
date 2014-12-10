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
package at.ac.tuwien.dsg.smartcom.services.rest;

import at.ac.tuwien.dsg.smartcom.exception.UnknownMessageException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.MessageInformation;
import at.ac.tuwien.dsg.smartcom.services.MessageInfoService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageInfoResource {

    @Inject
    private MessageInfoService mis;

    @GET
    public MessageInformation getMessageInformation(@QueryParam("type") String type,
                                                    @QueryParam("subtype") String subtype) {

        Message message = new Message.MessageBuilder().setType(type).setSubtype(subtype).create();

        try {
            return mis.getInfoForMessage(message);
        } catch (UnknownMessageException e) {
            throw new WebApplicationException(
                    "There is no message info for that query!",
                    Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @POST
    public Response postMessageInformation(MessageInformation info) {
        Message message = new Message.MessageBuilder()
                .setType(info.getKey().getType())
                .setSubtype(info.getKey().getSubtype())
                .create();
        mis.addMessageInfo(message, info);

        return Response.status(Response.Status.OK).build();
    }
}
