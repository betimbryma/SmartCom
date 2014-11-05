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

import java.io.Serializable;
import java.util.List;

/**
 * This class defines an address for a specific peer. It can provide several
 * parameters how to use an channelType to contact the peer (e.g., username, password etc.).
 *
 * An address is always related to a specific peer and a specific channelType.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerChannelAddress {
    private Identifier peerId; //id in the PM database
    private Identifier channelType; //type of the channel
    private List<? extends Serializable> contactParameters; //adapter specific contact/access parameters

    public PeerChannelAddress() {
    }

    public PeerChannelAddress(Identifier peerId, Identifier channelType, List<? extends Serializable> contactParameters) {
        this.peerId = peerId;
        this.channelType = channelType;
        this.contactParameters = contactParameters;
    }

    public Identifier getPeerId() {
        return peerId;
    }

    public void setPeerId(Identifier peerId) {
        this.peerId = peerId;
    }

    public Identifier getChannelType() {
        return channelType;
    }

    public void setChannelType(Identifier channelType) {
        this.channelType = channelType;
    }

    public List<? extends Serializable> getContactParameters() {
        return contactParameters;
    }

    public void setContactParameters(List<? extends Serializable> contactParameters) {
        this.contactParameters = contactParameters;
    }

    @Override
    public String toString() {
        return "PeerAddress{" +
                "peerId='" + peerId + '\'' +
                ", channelType='" + channelType + '\'' +
                ", contactParameters=" + contactParameters +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeerChannelAddress that = (PeerChannelAddress) o;

        if (channelType != null ? !channelType.equals(that.channelType) : that.channelType != null) return false;
        if (contactParameters != null ? !contactParameters.equals(that.contactParameters) : that.contactParameters != null)
            return false;
        if (peerId != null ? !peerId.equals(that.peerId) : that.peerId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = peerId != null ? peerId.hashCode() : 0;
        result = 31 * result + (channelType != null ? channelType.hashCode() : 0);
        result = 31 * result + (contactParameters != null ? contactParameters.hashCode() : 0);
        return result;
    }
}
