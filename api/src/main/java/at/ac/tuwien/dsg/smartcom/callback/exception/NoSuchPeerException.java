package at.ac.tuwien.dsg.smartcom.callback.exception;

import at.ac.tuwien.dsg.smartcom.model.Identifier;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class NoSuchPeerException extends Exception {
	public final Identifier requestedPeer;
	
	public NoSuchPeerException(Identifier requestedPeer){
		this.requestedPeer = requestedPeer;
	}
	
}
