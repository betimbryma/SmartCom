package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;

/**
* @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
* @version 1.0
*/
@Adapter(name="exception", stateful = true)
public class StatefulExceptionAdapter implements OutputAdapter {

    public StatefulExceptionAdapter(PeerAddress address) throws AdapterException {
        if (address.getContactParameters().size() > 0) {
            throw new AdapterException();
        }
    }

    @Override
    public void push(Message message, PeerAddress address) throws AdapterException {
        throw new AdapterException();
    }
}
