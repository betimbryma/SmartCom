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
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerInfo {
    private Identifier id;
    private DeliveryPolicy.Peer deliveryPolicy;
    private List<PrivacyPolicy> privacyPolicies;
    private List<PeerChannelAddress> addresses; //ordered list, ordering defines the preferences

    public PeerInfo(Identifier id, DeliveryPolicy.Peer deliveryPolicy, List<PrivacyPolicy> privacyPolicies, List<PeerChannelAddress> addresses) {
        this.id = id;
        this.deliveryPolicy = deliveryPolicy;
        this.privacyPolicies = privacyPolicies;
        this.addresses = addresses;
    }

    public Identifier getId() {
        return id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public DeliveryPolicy.Peer getDeliveryPolicy() {
        return deliveryPolicy;
    }

    public void setDeliveryPolicy(DeliveryPolicy.Peer deliveryPolicy) {
        this.deliveryPolicy = deliveryPolicy;
    }

    public List<PrivacyPolicy> getPrivacyPolicies() {
        return privacyPolicies;
    }

    public void setPrivacyPolicies(List<PrivacyPolicy> privacyPolicies) {
        this.privacyPolicies = privacyPolicies;
    }

    public List<PeerChannelAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<PeerChannelAddress> addresses) {
        this.addresses = addresses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeerInfo peerInfo = (PeerInfo) o;

        if (id != null ? !id.equals(peerInfo.id) : peerInfo.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PeerInfo{" +
                "id=" + id +
                ", deliveryPolicy=" + deliveryPolicy +
                ", privacyPolicies=" + privacyPolicies +
                ", addresses=" + addresses +
                '}';
    }
}
