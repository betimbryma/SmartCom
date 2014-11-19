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
package at.ac.tuwien.dsg.smartcom.integration;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
* Created by Philipp on 11.11.2014.
*/
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PeerManagerResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("collective/{id}")
    public String getCollectiveInfo(@PathParam("id") String id) {
        String body =
            "{" +
                "\"name\": [\"test\", \"test1\", \"test3\"]," +
                "\"owner\": {}," +
                "\"members\": [" +
                    "{ " +
                        "\"username\": \"test1\"," +
                        "\"password\": \"test1\"," +
                        "\"peerId\": 1," +
                        "\"mainProfileDefinitionId\": 2," +
                        "\"defaultPolicies\": []," +
                        "\"profileDefinitions\": []," +
                        "\"id\": 1" +
                    "}," +
                    "{ " +
                        "\"username\": \"test2\"," +
                        "\"password\": \"test2\"," +
                        "\"peerId\": 2," +
                        "\"mainProfileDefinitionId\": 3," +
                        "\"defaultPolicies\": []," +
                        "\"profileDefinitions\": []," +
                        "\"id\": 2" +
                    "}," +
                "]," +
                "\"id\": 1" +
            "}";
        return body;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("peer/{id}")
    public String getPeerInfo(@PathParam("id") String id) {
        String body =
                "{" +
                    "\"@type\": \"EQLSearchResult\"," +
                    "\"results\": ["+
                        "["+
                            "\"1\","+
                            "\"2\""+
                        "]"+
                    "]"+
                "}";
        return body;
    }

}
