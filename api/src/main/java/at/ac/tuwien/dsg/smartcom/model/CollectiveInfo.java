package at.ac.tuwien.dsg.smartcom.model;

import java.util.List;

/**
 * Created by Philipp on 12.08.2014.
 */
public class CollectiveInfo {
    private Identifier id;
    private List<Identifier> peers;
    private DeliveryPolicy.Collective deliveryPolicy;

    public CollectiveInfo(Identifier id, List<Identifier> peers, DeliveryPolicy.Collective deliveryPolicy) {
        this.id = id;
        this.peers = peers;
        this.deliveryPolicy = deliveryPolicy;
    }

    public CollectiveInfo() {}

    public Identifier getId() {
        return id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public List<Identifier> getPeers() {
        return peers;
    }

    public void setPeers(List<Identifier> peers) {
        this.peers = peers;
    }

    public DeliveryPolicy.Collective getDeliveryPolicy() {
        return deliveryPolicy;
    }

    public void setDeliveryPolicy(DeliveryPolicy.Collective deliveryPolicy) {
        this.deliveryPolicy = deliveryPolicy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectiveInfo that = (CollectiveInfo) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CollectiveInfo{" +
                "id=" + id +
                ", peers=" + peers +
                ", deliveryPolicy=" + deliveryPolicy +
                '}';
    }
}
