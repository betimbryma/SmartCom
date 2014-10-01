package at.ac.tuwien.dsg.smartcom.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import at.ac.tuwien.dsg.smartcom.utils.PropertiesLoader;

import javax.mail.MessagingException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class EmailAdapterTest {

    public static void main(String[] args) throws Exception {
        String recipient = null;
        String subject = null;
        String message = null;

//        System.out.println("Please insert the recipient of the message:");
//        recipient = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
//
//        System.out.println("Please insert the subject of the message:");
//        subject = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
//
//        System.out.println("Please insert the message of the message:");
//        message = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();

        testsendEmailAdapter(recipient, subject, message);
    }

    private static void testsendEmailAdapter(String recipient, String subject, String message) throws MessagingException, AdapterException {
//        MailUtils.sendMail(recipient, subject, message);

        List<Serializable> parameters = new ArrayList<>(2);
        parameters.add(getProperty("username"));
        PeerChannelAddress address = new PeerChannelAddress(Identifier.peer("test"), Identifier.adapter("Email"), parameters);

        final Message msg = new Message.MessageBuilder()
                .setId(Identifier.message("testId"))
                .setContent("testContent")
                .setType("testType")
                .setSubtype("testSubType")
                .setSenderId(Identifier.peer("sender"))
                .setReceiverId(Identifier.peer("receiver"))
                .setConversationId(""+System.nanoTime())
                .setTtl(3)
                .setLanguage("testLanguage")
                .setSecurityToken("securityToken")
                .create();

        EmailOutputAdapter adapter = new EmailOutputAdapter();
        adapter.push(msg, address);
        System.out.println("Email sent!");

        System.out.println("Email Adapter: Connecting...");
        EmailInputAdapter input = new EmailInputAdapter(msg.getConversationId(), getProperty("hostIncoming"), getProperty("username"), getProperty("password"),
                Integer.valueOf(getProperty("portIncoming")), true, "test", "test", true);
        Message inputMsg = input.pull();
        if (inputMsg != null) {
            System.out.println("email found on account");
        } else {
            System.err.println("email NOT found on account");
        }
    }

    private static String getProperty(String name) {
        return PropertiesLoader.getProperty("EmailAdapter.properties", name);
    }
}