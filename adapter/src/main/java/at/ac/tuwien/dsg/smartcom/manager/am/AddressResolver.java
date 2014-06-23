package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.model.PeerAddress;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class AddressResolver {

    Map<AddressKey, PeerAddress> addresses = Collections.synchronizedMap(new HashMap<AddressKey, PeerAddress>());

    AddressResolver() {
    }

    public PeerAddress getPeerAddress(String peerId, String adapterId) {
        return addresses.get(new AddressKey(peerId, adapterId));
    }

    public void addPeerAddress(PeerAddress address) {
        addresses.put(new AddressKey(address.getPeerId(), "adapter."+address.getAdapter()), address);
    }

    private class AddressKey {
        final String peerId;
        final String adapterId;

        private AddressKey(String peerId, String adapterId) {
            this.peerId = peerId;
            this.adapterId = adapterId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AddressKey that = (AddressKey) o;

            return adapterId.equals(that.adapterId) && peerId.equals(that.peerId);
        }

        @Override
        public int hashCode() {
            int result = peerId.hashCode();
            result = 31 * result + adapterId.hashCode();
            return result;
        }
    }
}
