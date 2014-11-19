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
package at.ac.tuwien.dsg.rest.adapter.resources;

import at.ac.tuwien.dsg.rest.adapter.AdapterRestService;
import at.ac.tuwien.dsg.smartcom.adapters.DropboxInputAdapter;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Path("dropbox")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DropboxAdapterResource {

    @Inject
    private AdapterRestService adapterRestService;

    @POST
    public String createEmailAdapter(DropboxAdapterConfig config,
                                     @DefaultValue("1000") @QueryParam("interval") long interval,
                                     @DefaultValue("true") @QueryParam("delete") boolean deleteIfSuccessful) {
        Message returnMessage = new Message.MessageBuilder()
                .setType(config.type)
                .setSubtype(config.subtype)
                .setSenderId(Identifier.component("dropbox"))
                .setConversationId(config.conversationId)
                .create();

        DropboxInputAdapter adapter = new DropboxInputAdapter(config.dropboxKey, config.dropboxFolder, config.fileName, returnMessage);

        return adapterRestService.addPullAdapter(adapter, interval, deleteIfSuccessful).getId();
    }

    private static class DropboxAdapterConfig {
        String dropboxKey;
        String dropboxFolder;
        String fileName;

        String type;
        String subtype;
        String conversationId;
    }
}
