package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.manager.am.utils.AdapterTestQueue;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;

/**
* @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
* @version 1.0
*/
@Adapter(name="stateless")
public class StatelessAdapter implements OutputAdapter {

    @Override
    public void push(Message message, PeerChannelAddress address) {
        AdapterTestQueue.publish(address.getPeerId().getId()+"."+address.getChannelType().getId(), message);
    }
}
