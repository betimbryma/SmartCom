package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.PeerAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.manager.utils.AdapterTestQueue;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;

/**
* @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
* @version 1.0
*/
@Adapter(name="stateful", stateful = true)
public class StatefulAdapter implements PeerAdapter {

    @Override
    public void push(Message message, PeerAddress address) {
        AdapterTestQueue.publish("stateful." + address.getPeerId(), message);
    }
}
