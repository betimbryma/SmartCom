package at.ac.tuwien.dsg.smartcom.messaging.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;

/**
* @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
* @version 1.0
*/
public class AdapterWithoutAnnotation implements OutputAdapter {

    @Override
    public void push(Message message, PeerChannelAddress address) {
        AdapterTestQueue.publish("stateless." + address.getPeerId().getId(), message);
    }
}
