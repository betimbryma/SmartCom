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
package at.ac.tuwien.dsg.smartcom.demo;

import at.ac.tuwien.dsg.smartcom.Communication;
import at.ac.tuwien.dsg.smartcom.SmartCom;
import at.ac.tuwien.dsg.smartcom.SmartComBuilder;
import at.ac.tuwien.dsg.smartcom.adapters.EmailInputAdapter;
import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.model.*;
import at.ac.tuwien.dsg.smartcom.utils.PropertiesLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class HelloWorldDemo implements NotificationCallback {

    public static void main(String[] args) throws Exception {
        String email;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        if (args.length == 0) {
            System.out.println("Please enter an email address: ");
            email = reader.readLine();
        } else {
            email = args[0];
        }

        SmartCom smartCom = helloWorld(email);

        System.out.println("Press enter to exit");

        reader.read();

        smartCom.tearDownSmartCom();

        System.out.println("Shutdown complete");
    }

    public static SmartCom helloWorld(String email) throws Exception {

        Identifier peerId = Identifier.peer("peer1");
        String conversation = "Hello World - " + UUID.randomUUID();

        DemoPeerManager peerManager = new DemoPeerManager();

        PeerInfo info = new PeerInfo();
        info.setId(peerId);
        info.setDeliveryPolicy(DeliveryPolicy.Peer.AT_LEAST_ONE);
        info.setPrivacyPolicies(null);

        PeerChannelAddress address = new PeerChannelAddress();
        address.setPeerId(peerId);
        address.setChannelType(Identifier.channelType("Email"));
        address.setContactParameters(Arrays.asList(email));
        info.setAddresses(Arrays.asList(address));
        peerManager.addPeer(peerId, info, peerId.getId());

        SmartCom smartCom = new SmartComBuilder(peerManager, peerManager, peerManager).create();

        //get communication API
        Communication communication = smartCom.getCommunication();

        //register the notification callback API
        communication.registerNotificationCallback(new HelloWorldDemo());

        //register the input handler (pulls every second)
        communication.addPullAdapter(
                new EmailInputAdapter(conversation,
                        PropertiesLoader.getProperty("EmailAdapter.properties", "hostIncoming"),
                        PropertiesLoader.getProperty("EmailAdapter.properties", "username"),
                        PropertiesLoader.getProperty("EmailAdapter.properties", "password"),
                        Integer.valueOf(PropertiesLoader.getProperty("EmailAdapter.properties", "portIncoming")),
                        true, "test", "test", true),
                1000);

        //create message
        Message.MessageBuilder builder =
                new Message.MessageBuilder()
                        .setType("TASK")
                        .setSubtype("REQUEST")
                        .setReceiverId(peerId)
                        .setSenderId(Identifier.component("DEMO"))
                        .setConversationId(conversation)
                        .setContent("Hello World!");
        Message msg = builder.create();

        //send the message
        communication.send(msg);

        return smartCom;
    }

    public void notify(Message message) {
        System.out.println("Received:" + message.toString());
    }
}
