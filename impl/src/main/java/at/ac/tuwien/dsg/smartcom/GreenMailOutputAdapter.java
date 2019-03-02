package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import at.ac.tuwien.dsg.smartcom.utils.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Adapter(name = "Email", stateful = false)
public class GreenMailOutputAdapter implements OutputAdapter {

    private static final Session session = getMailSession();
    private static String username;
    private static String password;

    private static final Logger log = LoggerFactory.getLogger(GreenMailOutputAdapter.class);

    @Override
    public void push(Message message, PeerChannelAddress address) throws AdapterException {

        if(address.getContactParameters().size() == 0){
            log.error("Peer address does not provide the required email address");
            throw new AdapterException();
        }

        String recipient = (String) address.getContactParameters().get(0);

        try {
            sendMail(recipient, message.getConversationId(), message.getContent());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private static void sendMail(String recipient, String subject, String message) throws MessagingException {
        javax.mail.Message msg = new MimeMessage(session);

        InternetAddress address = new InternetAddress(recipient);
        msg.setRecipient(javax.mail.Message.RecipientType.TO, address);

        msg.setSubject(subject);
        msg.setContent(message, "text/plain");
        msg.setFrom(new InternetAddress(username));

        Transport transport = session.getTransport("smtp");
        transport.connect(username, password);
        transport.sendMessage(msg, msg.getAllRecipients());
        transport.close();

    }

    private static Session getMailSession(){
        final Properties properties = new Properties();

        username = PropertiesLoader.getProperty("EmailAdapter.properties", "username");
        password = PropertiesLoader.getProperty("EmailAdapter.properties", "password");

        properties.setProperty("mail.smtp.host", PropertiesLoader.getProperty("EmailAdapter.properties", "hostOutgoing"));
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.port", PropertiesLoader.getProperty("EmailAdapter.properties", "portOutgoing"));
        properties.setProperty("mail.smtp.socketFactory.class", "com.icegreen.greenmail.util.DummySSLSocketFactory");
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        properties.setProperty("mail.from", username);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        return session;

    }
}