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
package at.ac.tuwien.dsg.pm;

import at.ac.tuwien.dsg.pm.dao.MongoDBCollectiveDAO;
import at.ac.tuwien.dsg.pm.dao.MongoDBPeerDAO;
import com.mongodb.MongoClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerManagerLauncher {

    public static void main(String[] args) throws IOException {
        int port;
        String uriPrefix = "PeerManager";
        String mongoDBHost = "localhost";
        int mongoDBPort = 12345;

        if (args.length == 0) {
            port = getFreePort();
        } else if (args.length == 1) {
            port = Integer.valueOf(args[1]);
        } else  {
            uriPrefix = args[0];
            port = Integer.valueOf(args[1]);

            if (args.length == 4) {
                mongoDBHost = args[2];
                mongoDBPort = Integer.valueOf(args[3]);
            }
        }

        PeerManager manager = startPeerManager(port, uriPrefix, mongoDBHost, mongoDBPort);

        System.out.println("Press enter to shutdown the peer manager");
        System.in.read();

        manager.cleanUp();
    }

    public static PeerManager startPeerManager(int port, String uriPrefix, MongoClient mongo) throws UnknownHostException {
        System.out.println("Running the peer manager on port ["+port+"] and path '"+uriPrefix+"'");

        MongoDBPeerDAO peerDAO = new MongoDBPeerDAO(mongo, "PM", "PEER");
        MongoDBCollectiveDAO collectiveDAO = new MongoDBCollectiveDAO(mongo, "PM", "COLLECTIVE");

        PeerManager manager = new PeerManager(port, uriPrefix, peerDAO, collectiveDAO);
        manager.init();
        return manager;
    }

    public static PeerManager startPeerManager(int port, String uriPrefix, String mongoDBHost, int mongoDBPort) throws UnknownHostException {
        return startPeerManager(port, uriPrefix, new MongoClient(mongoDBHost, mongoDBPort));
    }

    public static int getFreePort() {
        try {
            try (ServerSocket socket = new ServerSocket(0)) {
                socket.setReuseAddress(true);
                return socket.getLocalPort();
            }
        } catch (IOException e) {
            return -1;
        }
    }
}
