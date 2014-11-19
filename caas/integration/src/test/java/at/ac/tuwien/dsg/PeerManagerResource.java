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
package at.ac.tuwien.dsg;

import at.ac.tuwien.dsg.smartcom.model.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PeerManagerResource {

    @GET
    @Path("/collectiveInfo/{id}")
    public CollectiveInfo getCollectiveInfo(@PathParam("id") String id) {
        CollectiveInfo info = new CollectiveInfo();
        info.setId(Identifier.collective(id));
        info.setDeliveryPolicy(DeliveryPolicy.Collective.TO_ALL_MEMBERS);
        info.setPeers(Arrays.asList(Identifier.peer("1"), Identifier.peer("2")));
        return info;
    }

    @GET
    @Path("/peerInfo/{id}")
    public PeerInfo getPeerInfo(@PathParam("id") String id) {
        PeerInfo info = new PeerInfo();
        info.setId(Identifier.peer(id));
        info.setDeliveryPolicy(DeliveryPolicy.Peer.AT_LEAST_ONE);
        info.setPrivacyPolicies(null);
        info.setAddresses(Collections.<PeerChannelAddress>emptyList());
        return info;
    }

    @GET
    @Path("/peerAuth/{id}")
    public boolean authenticatePeer(@PathParam("id") String id, @HeaderParam("password") String password) {
        return id.equals(password);
    }
}
