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
import at.ac.tuwien.dsg.pm.exceptions.CollectiveAlreadyExistsException;
import at.ac.tuwien.dsg.pm.model.Collective;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;
import java.util.UUID;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Path("collective")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CollectiveResource {

    ObjectMapper mapper = new ObjectMapper();

    @Inject
    private PeerManager manager;

    @GET
    @Path("/{id}")
    public Response getCollective(@PathParam("id") String id) {
        Collective obj = manager.getCollective(id);

        if (obj != null) {
            return Response.ok(obj).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }


    @POST
    public Collective addCollective(Collective collective) throws CollectiveAlreadyExistsException {
        return manager.addCollective(collective);
    }

    @GET
    @Path("/all")
    public List<Collective> getAll() {
        return manager.getAllCollectives();
    }

    @PUT
    public Response updateCollective(Collective collective) {
        Collective obj = manager.updateCollective(collective);

        if (obj != null) {
            return Response.ok(obj).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("/{collectiveId}/{peerId}")
    public Response addPeerToCollective(@PathParam("collectiveId") String collectiveId, @PathParam("peerId") String peerId) {
        Collective obj = manager.addPeerToCollective(collectiveId, peerId);

        if (obj != null) {
            return Response.ok(obj).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{collectiveId}/{peerId}")
    public Response removePeerToCollective(@PathParam("collectiveId") String collectiveId, @PathParam("peerId") String peerId) {
        Collective obj = manager.removePeerToCollective(collectiveId, peerId);

        if (obj != null) {
            return Response.ok(obj).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCollective(@PathParam("id") String id) {
        Collective obj = manager.deleteCollective(id);

        if (obj != null) {
            return Response.ok(obj).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/all")
    public Response deleteAll() {
        manager.clearCollectiveData();
        return Response.ok().build();
    }

    @GET
    @Path("/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile() throws IOException {
        File file = File.createTempFile("collective", UUID.randomUUID().toString());
        PrintWriter writer = new PrintWriter(file, "UTF-8");

        List<Collective> all = manager.getAllCollectives();

        for (Collective collective : all) {
            String s = mapper.writeValueAsString(collective);
            writer.println(s.replaceAll("\\s", ""));
        }

        writer.close();

        Response.ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=collective.dump");
        return response.build();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.WILDCARD)
    public Response uploadFile(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @DefaultValue("true") @QueryParam("delete") boolean delete) {

        if (delete) {
            manager.clearCollectiveData();
        }

        // save it
        try {
            handleFile(uploadedInputStream, fileDetail);
        } catch (Exception e) {
            return Response.status(501).build();
        }

        return Response.status(200).build();
    }

    private void handleFile(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(uploadedInputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            Collective collective = mapper.readValue(line, Collective.class);
            manager.addCollective(collective);
        }
    }
}
