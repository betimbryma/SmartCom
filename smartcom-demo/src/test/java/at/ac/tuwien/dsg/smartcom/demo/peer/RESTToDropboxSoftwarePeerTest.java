package at.ac.tuwien.dsg.smartcom.demo.peer;

import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.adapters.RESTOutputAdapter;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RESTToDropboxSoftwarePeerTest {

    public static void main(String[] args) throws IOException, AdapterException {
        System.out.println("Please insert the access token of a dropbox account:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String accesstoken = reader.readLine().trim();
        RESTToDropboxSoftwarePeer peer = new RESTToDropboxSoftwarePeer(9090, "peer", 10000, accesstoken, "SmartCom/peer");

        peer.initialize();

        RESTOutputAdapter adapter = new RESTOutputAdapter();



        List<Serializable> parameters = new ArrayList<>(2);
        parameters.add("http://localhost:"+9090+"/peer/");
        PeerChannelAddress address = new PeerChannelAddress(Identifier.peer("test"), Identifier.adapter("Android"), parameters);

        String message = null;
        System.out.println("Press enter a command (type HELP for information)!");

        outerLoop:
        while ((message = reader.readLine()) != null) {
            switch (message) {
                case "EXIT":
                    break outerLoop;
                case "MSG":
                    System.out.println("Sending message to peer");
                    Message msg = new Message.MessageBuilder()
                            .setConversationId(System.nanoTime()+"")
                            .setContent("test")
                            .setType("REQUEST")
                            .setSubtype("REQUEST")
                            .setSenderId(Identifier.component("test"))
                            .setReceiverId(Identifier.peer("peer"))
                            .setId(Identifier.message("msg1"))
                            .create();
                    adapter.push(msg, address);
                    continue;
                default:
                    System.out.println("UNKNOWN COMMAND!");
                    System.out.println("\tEXIT: exits the program");
                    System.out.println("\tMSG: sends a message to the peer");
            }
        }

        reader.close();
        peer.terminate();
    }

}