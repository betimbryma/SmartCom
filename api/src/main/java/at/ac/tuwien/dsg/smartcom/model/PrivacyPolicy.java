package at.ac.tuwien.dsg.smartcom.model;

/**
 * Created by Philipp on 12.08.2014.
 */
public interface PrivacyPolicy {

    public boolean condition(Message msg);
}
