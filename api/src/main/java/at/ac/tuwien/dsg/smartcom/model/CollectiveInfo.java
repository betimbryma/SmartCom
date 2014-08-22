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
package at.ac.tuwien.dsg.smartcom.model;

import java.util.List;

/**
 * Created by Philipp on 12.08.2014.
 */
public class CollectiveInfo {
    private Identifier id;
    private List<Identifier> peers;
    private DeliveryPolicy deliveryPolicy;

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

    public DeliveryPolicy getDeliveryPolicy() {
        return deliveryPolicy;
    }

    public void setDeliveryPolicy(DeliveryPolicy deliveryPolicy) {
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
