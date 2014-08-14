package at.ac.tuwien.dsg.smartcom.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;

/**
 * The Output Adapter API will be used to implement an adapter that can
 * send (push) messages to a peer. Therefore the push method has to be
 * implemented. Output Adapters will receive a message, transform this
 * message and push it to the peer over an external communication channel
 * (e.g., send the message to a web platform or a mobile application).
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface OutputAdapter {

    /**
     * Push a message to the peer. This method defines the handling of the
     * actual communication between the platform and the peer.
     *
     * @param message Message that should be sent to the peer.
     * @param address The adapter specific address of the peer. If the adapter is stateful, this address will be the same on every call.
     *
     * @throws AdapterException an exception occurred during the sending of a message
     */
    public void push(Message message, PeerChannelAddress address) throws AdapterException;
}
