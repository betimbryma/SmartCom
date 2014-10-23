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
import at.ac.tuwien.dsg.smartcom.adapters.DropboxInputAdapter;
import at.ac.tuwien.dsg.smartcom.adapters.EmailInputAdapter;
import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.demo.peer.RESTToDropboxSoftwarePeer;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.model.*;
import at.ac.tuwien.dsg.smartcom.utils.PropertiesLoader;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class Demo {

    public static final String softwarePeerRestPostfix = "peer";
    public static final int softwarePeerRestPortStart = 9090;
    public static final String DROPBOX_FOLDER = "SmartCom/peer";

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        print("This starts the demo application!\nBefore we start, some configuration is required");
        print();

        print("Please insert the access token of a Dropbox account so that we are able to upload files.");
        print("This key can be generated in the 'App Console' of any Dropbox account.");
        final String dropboxKey = reader.readLine().trim();
        print();

        print("How many software peer should be created? (default is 1)");
        int softwarePeers = 1;
        try {
            softwarePeers = Integer.valueOf(reader.readLine().trim());
        } catch (NumberFormatException e) {
            print("Illegal number, using default value (1)");
        }
        if (softwarePeers < 0) {
            softwarePeers = 1;
            print("Illegal number, using default value (1)");
        }
        print("They are all available using REST and will add files to the dropbox!");
        print();

        List<PeerInfo> peerInfos = new ArrayList<>();
        for (int i = 1; i <= softwarePeers; i++) {
            print("#################### SOFTWARE peer #"+i+" ####################");
            Identifier id = Identifier.peer("softwarePeer"+i);
            List<PeerChannelAddress> addresses = new ArrayList<>();
            print("Starting configuration for software peer #"+i+" (id is '"+id.getId()+"')");

            List<Serializable> parameters = new ArrayList<>(1);
            parameters.add("http://localhost:"+(softwarePeerRestPortStart+i-1)+"/"+softwarePeerRestPostfix+"/");
            PeerChannelAddress address = new PeerChannelAddress(id, Identifier.channelType("REST"), parameters);
            addresses.add(address);
            print("Generated REST address of peer");

            print("Please choose a delivery policy for this peer (Options are: 'ALL', 'ONE' (for at least onc), and 'PREFERRED' (default))");
            DeliveryPolicy.Peer deliveryPolicy;
            switch(reader.readLine().trim().toUpperCase()) {
                case "ALL":
                    deliveryPolicy = DeliveryPolicy.Peer.TO_ALL_CHANNELS;
                    break;
                case "ONE":
                    deliveryPolicy = DeliveryPolicy.Peer.AT_LEAST_ONE;
                    break;
                case "PREFERRED":
                    deliveryPolicy = DeliveryPolicy.Peer.PREFERRED;
                    break;
                default:
                    print("Unknown delivery policy, using default");
                    deliveryPolicy = DeliveryPolicy.Peer.PREFERRED;
            }
            peerInfos.add(new PeerInfo(id, deliveryPolicy, null, addresses));

            print("Creation/Generation of peer information for software peer #"+i+" successful");
            print();
        }


        print("How many human peers should be created? (default is 2)");
        int humanPeers = 2;
        try {
            humanPeers = Integer.valueOf(reader.readLine().trim());
        } catch (NumberFormatException e) {
            print("Illegal number, using default value (1)");
        }
        if (humanPeers < 0) {
            humanPeers = 1;
            print("Illegal number, using default value (1)");
        }
        print();

        for (int i = 1; i <= humanPeers; i++) {
            print("#################### HUMAN peer #"+i+" ####################");
            Identifier id = Identifier.peer("humanPeer"+i);
            List<PeerChannelAddress> addresses = new ArrayList<>();
            print("Starting configuration for human peer #"+i+" (id is '"+id.getId()+"')");
            print("Please enter the email address for this peer (press enter if there is no such address)");
            final String email = reader.readLine().trim();
            if (!email.isEmpty()) {
                List<Serializable> parameters = new ArrayList<>(1);
                parameters.add(email);
                PeerChannelAddress address = new PeerChannelAddress(id, Identifier.channelType("Email"), parameters);
                addresses.add(address);
            }

            print("Please start the Android device and the dedicated SmartComApp for this peer if this hasn't been done yet!");
            print("Please generate the registration token and provide it here (press enter if there is no such address)");
            final String androidKey = reader.readLine().trim();
            if (!androidKey.isEmpty()) {
                List<Serializable> parameters = new ArrayList<>(1);
                parameters.add(androidKey);
                PeerChannelAddress address = new PeerChannelAddress(id, Identifier.channelType("Android"), parameters);
                addresses.add(address);
            }

            print("Please choose a delivery policy for this peer (Options are: 'ALL', 'ONE' (for at least onc), and 'PREFERRED' (default))");
            DeliveryPolicy.Peer deliveryPolicy;
            switch(reader.readLine().trim().toUpperCase()) {
                case "ALL":
                    deliveryPolicy = DeliveryPolicy.Peer.TO_ALL_CHANNELS;
                    break;
                case "ONE":
                    deliveryPolicy = DeliveryPolicy.Peer.AT_LEAST_ONE;
                    break;
                case "PREFERRED":
                    deliveryPolicy = DeliveryPolicy.Peer.PREFERRED;
                    break;
                default:
                    print("Unknown delivery policy, using default");
                    deliveryPolicy = DeliveryPolicy.Peer.PREFERRED;
            }
            peerInfos.add(new PeerInfo(id, deliveryPolicy, null, addresses));

            print();
        }

        print("CONFIGURATION DONE");
        if (peerInfos.size() == 0) {
            print("No peers available... can't start demo!");
            return;
        }

        try {
            startDemo(peerInfos, softwarePeers, humanPeers, dropboxKey);
        } catch (CommunicationException e) {
            e.printStackTrace();
        }
    }

    public static void startDemo(List<PeerInfo> info, int softwarePeers, int humanPeers, String dropboxKey) throws CommunicationException {
        print("Starting the demo application!");

        List<RESTToDropboxSoftwarePeer> softwarePeerList = new ArrayList<>();
        for (int i = 0; i < softwarePeers; i++) {
            RESTToDropboxSoftwarePeer peer = new RESTToDropboxSoftwarePeer(9090+i, "peer", 10000, dropboxKey, DROPBOX_FOLDER);
            peer.initialize();
            softwarePeerList.add(peer);
        }


        DemoPeerManager peerManager = new DemoPeerManager();
        for (PeerInfo peer : info) {
            peerManager.addPeer(peer.getId(), peer, peer.getId().getId());
        }

        SmartCom smartCom = new SmartCom(peerManager, peerManager, peerManager);
        smartCom.initializeSmartCom();
        Communication communication = smartCom.getCommunication();
        communication.registerNotificationCallback(new NotificationHandler());

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String message;
        List<String> collectives = new ArrayList<>();
        Map<String, PeerInfo> peerMap = new HashMap<>();
        List<String> peers = new ArrayList<>();
        for (PeerInfo peerInfo : info) {
            peerMap.put(peerInfo.getId().getId(), peerInfo);
            peers.add(peerInfo.getId().getId());
        }

        try {
            print("################ Demo ################");
            print("Select 'PEER' for the peer configuration, 'COLLECTIVE' to create a new collective, " +
                    "'MESSAGE' to create a new message, 'EXIT' to exit the demo, " +
                    "'HELP' or any other command for help information");
            outerLoop:
            while ((message = reader.readLine()) != null) {
                switch(message.toUpperCase()) {

                    case "PEER": { //otherwise we have naming conflicts...
                        print("################ Peer Configuration ################");
                        print("Select 'LIST' for a list of all peers, "+
                                "'EXIT' to exit the peer configuration, "+
                                "'HELP' or any other command for help information");
                        peerLoop:
                        while ((message = reader.readLine()) != null) {
                            switch (message.toUpperCase()) {
                                case "LIST":
                                    StringBuilder builder = new StringBuilder();
                                    for (String peer : peers) {
                                        builder.append(peer).append("\n");
                                    }
                                    System.out.print(builder.toString());
                                    break;

                                case "EXIT":
                                    break peerLoop;

                                default:
                                    print("\tLIST lists all peers");
                                    print("\tEXIT exits the collective configuration");
                                    break;
                            }
                        }
                    }
                        break;

                    case "COLLECTIVE": { //otherwise we have naming conflicts...
                        print("################ Collective Configuration ################");
                        print("Select 'LIST' for a list of all collectives, 'CREATE' to create a new collective, " +
                                "'ADD' to add a peer to a collective, 'REMOVE' to remove a peer from a collective or " +
                                "'EXIT' to exit the collective configuration, "+
                                "'HELP' or any other command for help information");
                        collectiveLoop:
                        while ((message = reader.readLine()) != null) {
                            switch (message.toUpperCase()) {
                                case "LIST":
                                    StringBuilder builder = new StringBuilder();
                                    for (String peer : collectives) {
                                        builder.append(peer).append("\n");
                                    }
                                    System.out.print(builder.toString());
                                    break;

                                case "CREATE": { //otherwise we have naming conflicts...
                                    print("Choose the name of the collective");
                                    String name = reader.readLine();
                                    if (collectives.contains(name)) {
                                        print("There is already such a collective");
                                        break;
                                    }

                                    print("Choose the delivery policy of the collective (options are 'ALL' and 'ANY' (default))");
                                    DeliveryPolicy.Collective deliveryPolicy = DeliveryPolicy.Collective.TO_ANY;
                                    switch (reader.readLine().toUpperCase()) {
                                        case "ALL":
                                            deliveryPolicy = DeliveryPolicy.Collective.TO_ALL_MEMBERS;
                                            break;
                                        case "ANY":
                                            deliveryPolicy = DeliveryPolicy.Collective.TO_ANY;
                                            break;
                                        default:
                                            print("unknown policy...");
                                            deliveryPolicy = DeliveryPolicy.Collective.TO_ANY;
                                    }
                                    peerManager.registerCollective(new CollectiveInfo(Identifier.collective(name), new ArrayList<Identifier>(), deliveryPolicy));
                                    print("Created new collective with id " + name + "\n");
                                    collectives.add(name);
                                }
                                    break;

                                case "ADD": { //otherwise we have naming conflicts...
                                    print("Choose the name of the collective");
                                    String collective = reader.readLine();
                                    if (!collectives.contains(collective)) {
                                        print("There is no such collective...");
                                        break;
                                    }
                                    print("Choose the name of the peer");
                                    String peer = reader.readLine();
                                    if (!peers.contains(peer)) {
                                        print("There is no such peer...");
                                        break;
                                    }
                                    peerManager.addPeerToCollective(Identifier.peer(peer), Identifier.collective(collective));
                                }
                                    break;

                                case "REMOVE": { //otherwise we have naming conflicts...
                                    print("Choose the name of the collective");
                                    String collective = reader.readLine();
                                    if (!collectives.contains(collective)) {
                                        print("There is no such collective...");
                                        break;
                                    }
                                    print("Choose the name of the peer");
                                    String peer = reader.readLine();
                                    if (!peers.contains(peer)) {
                                        print("There is no such peer...");
                                        break;
                                    }
                                    peerManager.removePeerFromCollective(Identifier.peer(peer), Identifier.collective(collective));
                                }
                                break;

                                case "EXIT":
                                    break collectiveLoop;

                                default:
                                    print("\tLIST lists all collectives");
                                    print("\tCREATE creates a new collective");
                                    print("\tADD add a peer to a collective");
                                    print("\tREMOVE remove a peer from a collective");
                                    print("\tEXIT exits the collective configuration");
                                    break;
                            }
                        }
                    }
                        break;

                    case "MESSAGE": {
                        print("################ Message Configuration ################");
                        print("Do you want to send the message to a peer or a collective? Options: 'COLL' for collective and peer otherwise");
                        String recipientType = reader.readLine().toUpperCase();

                        Identifier recipient;
                        print("Select the name of the peer/collective");
                        String name = reader.readLine();
                        boolean isCollective = false;
                        if ("COLL".equals(recipientType) && collectives.contains(name)) {
                            if (collectives.contains(name)) {
                                isCollective = true;
                                recipient = Identifier.collective(name);
                            } else {
                                print("There is no such collective");
                                break;
                            }
                        } else if (peers.contains(name)) {
                            recipient = Identifier.peer(name);
                        } else {
                            print("There is no such peer!");
                            break;
                        }

                        print("Select the TYPE of the message");
                        String type = reader.readLine();

                        print("Select the SUBTYPE of the message");
                        String subtype = reader.readLine();

                        print("Select the CONTENT of the message (Warning: a new line ends the selection)");
                        String content = reader.readLine();

                        Message.MessageBuilder builder = new Message.MessageBuilder()
                                .setType(type)
                                .setSubtype(subtype)
                                .setReceiverId(recipient)
                                .setSenderId(Identifier.component("DEMO"))
                                .setConversationId(System.nanoTime() + "")
                                .setContent(content);

                        print("Now you can specify further details of the message. Type HELP to get a list of commands!");
                        messageLoop:
                        while ((message = reader.readLine()) != null) {
                            switch (message.toUpperCase()) {
                                case "TYPE":
                                    print("Enter the type of the message:");
                                    builder.setType(reader.readLine());
                                    break;

                                case "SUBTYPE":
                                    print("Enter the subtype of the message:");
                                    builder.setSubtype(reader.readLine());
                                    break;

                                case "CONTENT":
                                    print("Enter the content of the message:");
                                    builder.setContent(reader.readLine());
                                    break;

                                case "CONVID":
                                    print("Enter the conversation id of the message:");
                                    builder.setConversationId(reader.readLine());
                                    break;

                                case "SEND":
                                    print("Message will be sent...");
                                    Message msg = builder.create();


                                    if (!isCollective) {
                                        PeerInfo peerInfo = peerMap.get(msg.getReceiverId().getId());
                                        for (PeerChannelAddress address : peerInfo.getAddresses()) {
                                            switch (address.getChannelType().getId()) {
                                                case "REST": //they should return a message to dropbox
                                                    Message returnMessage = new Message.MessageBuilder()
                                                            .setType(msg.getType())
                                                            .setSubtype("RESPONSE")
                                                            .setContent("This is a response!")
                                                            .setSenderId(Identifier.component("dropbox"))
                                                            .setConversationId(msg.getConversationId())
                                                            .create();
                                                    communication.addPullAdapter(new DropboxInputAdapter(dropboxKey, DROPBOX_FOLDER, "result_" + msg.getConversationId() + ".result", returnMessage), 1000, true);
                                                    break;
                                                case "Email": //they should return a message to the email
                                                    EmailInputAdapter input = new EmailInputAdapter(msg.getConversationId(),
                                                            PropertiesLoader.getProperty("EmailAdapter.properties", "hostIncoming"),
                                                            PropertiesLoader.getProperty("EmailAdapter.properties", "username"),
                                                            PropertiesLoader.getProperty("EmailAdapter.properties", "password"),
                                                            Integer.valueOf(PropertiesLoader.getProperty("EmailAdapter.properties", "portIncoming")),
                                                            true, "test", "test", true);
                                                    communication.addPullAdapter(input, 1000, true);
                                                    break;

                                                case "Android": //they should return a message to the REST interface

                                            }
                                        }
                                    }


                                    communication.send(msg);
                                    break messageLoop;

                                case "EXIT":
                                    break messageLoop;

                                default:
                                    print("\tEXIT exits the message configuration");
                                    print("\tSEND send the message");
                                    print();
                                    print("\tTYPE set the type of the message");
                                    print("\tSUBTYPE set the subtype of the message");
                                    print("\tCONTENT set the content of the message");
                                    print("\tCONVID set the conversation id of the message");
                                    break;
                            }
                        }

                        print("Message has been sent!");
                    }
                        break;

                    case "DEMO": {
                        print("################ Starting demo ################");
                        String collectiveName = "peerColl";
                        for (int i = 1; collectives.contains(collectiveName); i++) {
                            collectiveName = "peerColl" + i;
                        }
                        collectives.add(collectiveName);

                        DeliveryPolicy.Collective deliveryPolicy = DeliveryPolicy.Collective.TO_ALL_MEMBERS;
                        print("Creating collective '" + collectiveName + "' with deliveryPolicy 'TO_ALL_MEMBERS'");
                        ArrayList<Identifier> collectivePeers = new ArrayList<>();

                        print("Adding all available peers to the collective");
                        for (PeerInfo peerInfo : peerMap.values()) {
                            collectivePeers.add(peerInfo.getId());
                        }
                        Identifier coll1Id = Identifier.collective(collectiveName);
                        peerManager.registerCollective(new CollectiveInfo(coll1Id, collectivePeers, deliveryPolicy));


                        Identifier coll2Id = null;
                        if (softwarePeers > 0) {
                            collectiveName = "softwareColl";
                            for (int i = 1; collectives.contains(collectiveName); i++) {
                                collectiveName = "softwareColl" + i;
                            }
                            collectives.add(collectiveName);


                            deliveryPolicy = DeliveryPolicy.Collective.TO_ANY;
                            print("Creating collective '" + collectiveName + "' with deliveryPolicy 'TO_ANY'");
                            collectivePeers = new ArrayList<>();

                            print("Adding all SOFTWARE peers to the collective");
                            for (int i = 1; i <= softwarePeers; i++) {
                                collectivePeers.add(Identifier.peer("softwarePeer" + i));
                            }
                            coll2Id = Identifier.collective(collectiveName);
                            peerManager.registerCollective(new CollectiveInfo(coll2Id, collectivePeers, deliveryPolicy));
                        }

                        print();
                        if (softwarePeers > 0) {
                            print("Sending a message to a software peer");

                            Message.MessageBuilder builder = new Message.MessageBuilder()
                                    .setType("COMPUTE")
                                    .setSubtype("REQUEST")
                                    .setReceiverId(Identifier.peer("softwarePeer1"))
                                    .setSenderId(Identifier.component("DEMO"))
                                    .setConversationId(System.nanoTime() + "")
                                    .setContent("5000"); //defines how long the peer waits

                            Message msg = builder.create();

                            Message returnMessage = new Message.MessageBuilder()
                                    .setType(msg.getType())
                                    .setSubtype("RESPONSE")
                                    .setContent("Files have been added to Dropbox!")
                                    .setSenderId(Identifier.component("dropbox"))
                                    .setConversationId(msg.getConversationId())
                                    .create();
                            communication.addPullAdapter(new DropboxInputAdapter(dropboxKey, DROPBOX_FOLDER, "result_" + msg.getConversationId() + ".result", returnMessage), 1000, true);
                            communication.send(msg);
                            print("Message sent, lets wait for a response. Press enter if you are done and want to continue.");
                            reader.readLine();
                            print();
                        }

                        if (humanPeers > 0) {
                            print("Sending a message to a human peer");

                            Message.MessageBuilder builder = new Message.MessageBuilder()
                                    .setType("DEMO")
                                    .setSubtype("NOTIFICATION")
                                    .setReceiverId(Identifier.peer("humanPeer1"))
                                    .setSenderId(Identifier.component("DEMO"))
                                    .setConversationId(System.nanoTime() + "")
                                    .setContent("Please respond to this message immediately with a blank text!");

                            Message msg = builder.create();
                            communication.send(msg);

                            EmailInputAdapter input = new EmailInputAdapter(msg.getConversationId(),
                                    PropertiesLoader.getProperty("EmailAdapter.properties", "hostIncoming"),
                                    PropertiesLoader.getProperty("EmailAdapter.properties", "username"),
                                    PropertiesLoader.getProperty("EmailAdapter.properties", "password"),
                                    Integer.valueOf(PropertiesLoader.getProperty("EmailAdapter.properties", "portIncoming")),
                                    true, "test", "test", true);
                            communication.addPullAdapter(input, 1000, true);
                            print("Message sent, lets wait for a response. Press enter if you are done and want to continue.");
                            reader.readLine();
                            print();
                        }

                        if (coll2Id != null) {
                            print("Sending a message to the collective consisting of all softwarePeers. The first one responding 'wins'.");

                            Message.MessageBuilder builder = new Message.MessageBuilder()
                                    .setType("COMPUTE")
                                    .setSubtype("REQUEST")
                                    .setReceiverId(coll2Id)
                                    .setSenderId(Identifier.component("DEMO"))
                                    .setConversationId(System.nanoTime() + "")
                                    .setContent("5000"); //defines how long the peer waits

                            Message msg = builder.create();

                            Message returnMessage = new Message.MessageBuilder()
                                    .setType(msg.getType())
                                    .setSubtype("RESPONSE")
                                    .setContent("Files have been added to Dropbox!")
                                    .setSenderId(Identifier.component("dropbox"))
                                    .setConversationId(msg.getConversationId())
                                    .create();
                            communication.addPullAdapter(new DropboxInputAdapter(dropboxKey, DROPBOX_FOLDER, "result_" + msg.getConversationId() + ".result", returnMessage), 1000, true);
                            communication.send(msg);
                            print("Message sent, lets wait for a response. Press enter if you are done and want to continue.");
                            reader.readLine();
                            print();
                        }

                        if (coll1Id != null) {
                            print("Sending a message to the collective consisting of all softwarePeers. The first one responding 'wins'.");

                            Message.MessageBuilder builder = new Message.MessageBuilder()
                                    .setType("COMPUTE")
                                    .setSubtype("REQUEST")
                                    .setReceiverId(coll1Id)
                                    .setSenderId(Identifier.component("DEMO"))
                                    .setConversationId(System.nanoTime() + "")
                                    .setContent("Do some stuff and respond!");

                            Message msg = builder.create();

                            if (softwarePeers > 0) {
                                Message returnMessage = new Message.MessageBuilder()
                                        .setType(msg.getType())
                                        .setSubtype("RESPONSE")
                                        .setContent("Files have been added to Dropbox!")
                                        .setSenderId(Identifier.component("dropbox"))
                                        .setConversationId(msg.getConversationId())
                                        .create();
                                communication.addPullAdapter(new DropboxInputAdapter(dropboxKey, DROPBOX_FOLDER, "result_" + msg.getConversationId() + ".result", returnMessage), 1000, true);
                            }
                            if (humanPeers > 0) {
                                EmailInputAdapter input = new EmailInputAdapter(msg.getConversationId(),
                                        PropertiesLoader.getProperty("EmailAdapter.properties", "hostIncoming"),
                                        PropertiesLoader.getProperty("EmailAdapter.properties", "username"),
                                        PropertiesLoader.getProperty("EmailAdapter.properties", "password"),
                                        Integer.valueOf(PropertiesLoader.getProperty("EmailAdapter.properties", "portIncoming")),
                                        true, "test", "test", true);
                                communication.addPullAdapter(input, 1000, true);
                            }
                            communication.send(msg);
                            print("Message sent, lets wait for a response. Press enter if you are done and want to continue.");
                            reader.readLine();
                            print();
                        }


                        print("Press ENTER to end the demo");
                        reader.read();

                        print("################ Demo ended ################");
                    }
                        break;

                    case "EXIT":
                        break outerLoop;

                    default:
                        print("\tPEER for the peer configuration");
                        print("\tCOLLECTIVE to create a new collective");
                        print("\tMESSAGE to create a new message");
                        print("\tEXIT to exit the demo");
                        print("\tDEMO starts the predefined demo");
                        print("\tHELP or any other command for help information");
                        break;
                }
                print("################ Demo ################");
            }
        } catch (IOException ignored) {}

        for (RESTToDropboxSoftwarePeer peer : softwarePeerList) {
            peer.terminate();
        }

        smartCom.tearDownSmartCom();
    }

    private static class NotificationHandler implements NotificationCallback {

        @Override
        public void notify(Message message) {
            StringBuilder builder = new StringBuilder();
            builder.append("################ New Message received ################").append("\n");
            builder.append(message.toString()).append("\n");
            builder.append("################                      ################");

            createWindow("New Message received: "+message.getContent());

            print(builder.toString());
        }
    }

    private static void print(String print) {
        System.out.println(print);
    }

    private static void print() {
        print("");
    }

    private static JFrame createWindow(String message) {
        //Create and set up the window.
        JFrame frame = new JFrame("SmartCom Demo");
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        JLabel textLabel = new JLabel(message, SwingConstants.CENTER);
        textLabel.setPreferredSize(new Dimension(300, 100));
        frame.getContentPane().add(textLabel, BorderLayout.CENTER);
        //Display the window.
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
        return frame;
    }
}
