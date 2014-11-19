/**
 * Copyright (c) 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184 (http://dsg.tuwien.ac.at)
 *
 * This work was partially supported by the EU FP7 FET SmartSociety (http://www.smart-society-project.eu/).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package at.ac.tuwien.dsg.pm.model;

import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class Collective {
    private String id;
    private List<String> peers = new ArrayList<String>();
    private DeliveryPolicy.Collective deliveryPolicy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getPeers() {
        return peers;
    }

    public void addPeer(String peer) {
        removePeer(peer);
        this.peers.add(peer);
    }

    public void removePeer(String peer) {
        this.peers.remove(peer);
    }

    public void setPeers(List<String> peers) {
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

        Collective that = (Collective) o;

        if (deliveryPolicy != that.deliveryPolicy) return false;
        if (!id.equals(that.id)) return false;
        if (peers != null ? !peers.equals(that.peers) : that.peers != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (peers != null ? peers.hashCode() : 0);
        result = 31 * result + (deliveryPolicy != null ? deliveryPolicy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Collective{" +
                "id='" + id + '\'' +
                ", peers=" + peers +
                ", deliveryPolicy=" + deliveryPolicy +
                '}';
    }
}
