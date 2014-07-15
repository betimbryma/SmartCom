package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.utils.AdapterTestQueue;
import at.ac.tuwien.dsg.smartcom.model.Message;

/**
* @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
* @version 1.0
*/
public class TestInputPullAdapter implements InputPullAdapter {
    private final String pullAddress;

    public TestInputPullAdapter(String pullAddress) {
        this.pullAddress = pullAddress;
    }

    @Override
    public Message pull() {
        return AdapterTestQueue.receive(pullAddress);
    }
}
