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

import at.ac.tuwien.dsg.peer.PeerMailboxService;
import at.ac.tuwien.dsg.peer.PeerMailboxServiceLauncher;
import at.ac.tuwien.dsg.pm.PeerManager;
import at.ac.tuwien.dsg.pm.PeerManagerLauncher;
import at.ac.tuwien.dsg.rest.adapter.AdapterRestService;
import at.ac.tuwien.dsg.smartcom.SmartCom;
import at.ac.tuwien.dsg.smartcom.SmartComBuilder;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;

import java.io.IOException;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class Launcher {

    public static void main(String[] args) throws IOException, PeerAuthenticationException, CommunicationException {

        MongoDBLauncher.MongoDBInstance mongodb = MongoDBLauncher.startMongoDB(12345, "storage/mongoDB");

        PeerManager peerManager = PeerManagerLauncher.startPeerManager(8080, "PeerManager", mongodb.getClient());

        PeerManagerConnector peerManagerConnector = new PeerManagerConnector("http://localhost:8080/PeerManager");

        PeerMailboxService mailboxService = PeerMailboxServiceLauncher.startPeerManager(8083, "mailbox", mongodb.getClient());
        System.out.println("Running the the peer mailbox service on port ["+8083+"] and path 'mailbox'");

        SmartCom smartCom = new SmartComBuilder(peerManagerConnector, peerManagerConnector, peerManagerConnector)
                .initAdapters(true)
                .initializeActiveMQ(true)
                .setMongoClient(mongodb.getClient())
                .setRestApiPort(8081)
                .useLocalMessageQueue(true)
                .create();
        System.out.println("Running the the SmartCom rest service on port ["+8081+"] and path 'SmartCom'");

        AdapterRestService adapterRestService = new AdapterRestService(8082, "SmartCom/adapter", smartCom.getCommunication());
        adapterRestService.init();
        System.out.println("Running the the adapter rest service on port ["+8082+"] and path 'SmartCom/adapter'");

        System.out.println("Press enter to shutdown the application");
        System.in.read();

        adapterRestService.cleanUp();
        smartCom.tearDownSmartCom();
        mailboxService.cleanUp();
        peerManager.cleanUp();
        mongodb.tearDown();
    }
}
