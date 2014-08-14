package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.utils.AdapterTestQueue;
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
