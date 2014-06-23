package at.ac.tuwien.dsg.smartcom.model;

import java.io.Serializable;
import java.util.List;

/**
 * This class defines an address for a specific peer. It can provide several
 * parameters how to use an adapter to contact the peer (e.g., username, password etc.).
 *
 * An address is always related to a specific peer and a specific adapter.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerAddress {
    private String peerId;
    private String adapter;
    private List<Serializable> contactParameters;

    public PeerAddress(String peerId, String adapter, List<Serializable> contactParameters) {
        this.peerId = peerId;
        this.adapter = adapter;
        this.contactParameters = contactParameters;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getAdapter() {
        return adapter;
    }

    public void setAdapter(String adapter) {
        this.adapter = adapter;
    }

    public List<Serializable> getContactParameters() {
        return contactParameters;
    }

    public void setContactParameters(List<Serializable> contactParameters) {
        this.contactParameters = contactParameters;
    }

    @Override
    public String toString() {
        return "PeerAddress{" +
                "peerId='" + peerId + '\'' +
                ", adapter='" + adapter + '\'' +
                ", contactParameters=" + contactParameters +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeerAddress that = (PeerAddress) o;

        if (adapter != null ? !adapter.equals(that.adapter) : that.adapter != null) return false;
        if (contactParameters != null ? !contactParameters.equals(that.contactParameters) : that.contactParameters != null)
            return false;
        if (peerId != null ? !peerId.equals(that.peerId) : that.peerId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = peerId != null ? peerId.hashCode() : 0;
        result = 31 * result + (adapter != null ? adapter.hashCode() : 0);
        result = 31 * result + (contactParameters != null ? contactParameters.hashCode() : 0);
        return result;
    }
}
