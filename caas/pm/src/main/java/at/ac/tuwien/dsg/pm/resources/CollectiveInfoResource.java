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
package at.ac.tuwien.dsg.pm.resources;

import at.ac.tuwien.dsg.pm.PeerManager;
import at.ac.tuwien.dsg.pm.model.Collective;
import at.ac.tuwien.dsg.smartcom.model.CollectiveInfo;
import at.ac.tuwien.dsg.smartcom.model.Identifier;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Path("collectiveInfo")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CollectiveInfoResource {

    @Inject
    private PeerManager manager;

    @GET
    @Path("/{id}")
    public CollectiveInfo getCollectiveInfo(@PathParam("id") String id) {
        Collective collective = manager.getCollective(id);

        if (collective == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }

        CollectiveInfo info = new CollectiveInfo();
        info.setId(Identifier.collective(id));
        info.setDeliveryPolicy(collective.getDeliveryPolicy());

        List<Identifier> peers = new ArrayList<>(collective.getPeers().size());
        for (String s : collective.getPeers()) {
            peers.add(Identifier.peer(s));
        }
        info.setPeers(peers);

        return info;
    }
}
