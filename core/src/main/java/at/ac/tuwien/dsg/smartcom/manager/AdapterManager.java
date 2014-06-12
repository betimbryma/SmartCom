package at.ac.tuwien.dsg.smartcom.manager;

import at.ac.tuwien.dsg.smartcom.adapter.FeedbackAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.PeerAdapter;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface AdapterManager {
    void init();

    void destroy();

    String addPushAdapter(FeedbackPushAdapter adapter);

    String addPullAdapter(FeedbackPullAdapter adapter);

    FeedbackAdapter removeAdapter(String adapterId);

    String registerPeerAdapter(Class<? extends PeerAdapter> adapter);

    void removePeerAdapter(String adapterId);

    public RoutingRule createEndpointForPeer(String peerId);
}
