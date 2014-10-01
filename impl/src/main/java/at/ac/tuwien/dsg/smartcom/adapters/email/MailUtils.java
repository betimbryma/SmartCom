package at.ac.tuwien.dsg.smartcom.adapters.email;

import at.ac.tuwien.dsg.smartcom.utils.PropertiesLoader;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MailUtils {

    private static final Session session = getMailSession();
    private static String username;
    private static String password;

    private static Session getMailSession(){
        final Properties props = new Properties();

        username = PropertiesLoader.getProperty("EmailAdapter.properties", "username");
        password = PropertiesLoader.getProperty("EmailAdapter.properties", "password");

        props.setProperty("mail.smtp.host", PropertiesLoader.getProperty("EmailAdapter.properties", "hostOutgoing"));
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.port", PropertiesLoader.getProperty("EmailAdapter.properties", "portOutgoing"));
        props.setProperty( "mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory" );
        props.setProperty( "mail.smtp.socketFactory.fallback", "false" );
        props.setProperty("mail.from", username);
//        props.setProperty("mail.sender", username);
//        props.setProperty("mail.debug", "true");

        Session session =  Session.getInstance(props, new javax.mail.Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username,password);
            }
        });
//        session.setDebug(true);
        return session;
    }

    public static void sendMail(String recipient, String subject, String message) throws MessagingException {
        Message msg = new MimeMessage(session);

        InternetAddress addressTo = new InternetAddress(recipient);
        msg.setRecipient(Message.RecipientType.TO, addressTo);
        msg.setRecipient(Message.RecipientType.TO, addressTo);

        msg.setSubject(subject);
        msg.setContent(message, "text/plain");
        msg.setFrom(new InternetAddress(username));

        Transport t = session.getTransport("smtp");
        t.connect(username, password);
        t.sendMessage(msg, msg.getAllRecipients());
        t.close();
    }

    public static Folder getMail(String subject, String host, final String username, final String password, int port, boolean authentication) throws MessagingException {
        final Properties props = new Properties();

        props.setProperty("mail.pop3.host", host);
        props.setProperty("mail.pop3.user", username);
        props.setProperty("mail.pop3.password", password);
        props.setProperty("mail.pop3.port", Integer.toString(port));
        props.setProperty("mail.pop3.auth", Boolean.toString(authentication));
        props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session =  Session.getInstance(props, new javax.mail.Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username,password);
            }
        });

        Store store = session.getStore("pop3");
        store.connect();

        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);

        return folder;
    }

    public static void close(Folder folder) throws MessagingException {
        folder.close(false);
        folder.getStore().close();
    }
}
