package at.ac.tuwien.dsg.smartcom.messaging;

import at.ac.tuwien.dsg.smartcom.callback.PeerInfoCallback;

/**
 * Service that provides peer information internally. Usually provides a cache of
 * already retrieved peer information from the peer manager.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface PeerInfoService extends PeerInfoCallback {
}
