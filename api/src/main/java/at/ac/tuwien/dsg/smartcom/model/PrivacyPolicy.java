package at.ac.tuwien.dsg.smartcom.model;

/**
 *
 * Defines a privacy policy that can decide whether it is
 * possible to send a message to a peer.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface PrivacyPolicy {

    public boolean condition(Message msg);
}
