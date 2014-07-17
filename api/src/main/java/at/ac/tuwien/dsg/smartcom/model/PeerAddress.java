package at.ac.tuwien.dsg.smartcom.model;

import java.io.Serializable;
import java.util.List;

/**
 * This class defines an address for a specific peer. It can provide several
 * parameters how to use an adapterId to contact the peer (e.g., username, password etc.).
 *
 * An address is always related to a specific peer and a specific adapterId.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerAddress {
    private Identifier peerId; //id in the PM database
    private Identifier adapterId; //internal id of the adapter
    private List<Serializable> contactParameters; //adapter specific contact/access parameters

    public PeerAddress(Identifier peerId, Identifier adapterId, List<Serializable> contactParameters) {
        this.peerId = peerId;
        this.adapterId = adapterId;
        this.contactParameters = contactParameters;
    }

    public Identifier getPeerId() {
        return peerId;
    }

    public void setPeerId(Identifier peerId) {
        this.peerId = peerId;
    }

    public Identifier getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(Identifier adapterId) {
        this.adapterId = adapterId;
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
                ", adapterId='" + adapterId + '\'' +
                ", contactParameters=" + contactParameters +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeerAddress that = (PeerAddress) o;

        if (adapterId != null ? !adapterId.equals(that.adapterId) : that.adapterId != null) return false;
        if (contactParameters != null ? !contactParameters.equals(that.contactParameters) : that.contactParameters != null)
            return false;
        if (peerId != null ? !peerId.equals(that.peerId) : that.peerId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = peerId != null ? peerId.hashCode() : 0;
        result = 31 * result + (adapterId != null ? adapterId.hashCode() : 0);
        result = 31 * result + (contactParameters != null ? contactParameters.hashCode() : 0);
        return result;
    }
}
