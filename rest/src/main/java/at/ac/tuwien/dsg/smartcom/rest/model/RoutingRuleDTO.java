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
package at.ac.tuwien.dsg.smartcom.rest.model;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class RoutingRuleDTO {

    private String type;
    private String subtype;
    private IdentifierDTO receiver;
    private IdentifierDTO sender;

    private IdentifierDTO route;

    public RoutingRuleDTO() {
    }

    public RoutingRuleDTO(RoutingRule rule) {
        this.type = rule.getType();
        this.subtype = rule.getSubtype();

        if (rule.getReceiver() != null) {
            this.receiver = new IdentifierDTO(rule.getReceiver());
        }

        if (rule.getSender() != null) {
            this.sender = new IdentifierDTO(rule.getSender());
        }

        if (rule.getRoute() != null) {
            this.route = new IdentifierDTO(rule.getRoute());
        }
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public IdentifierDTO getReceiver() {
        return receiver;
    }

    public IdentifierDTO getSender() {
        return sender;
    }

    public IdentifierDTO getRoute() {
        return route;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public void setReceiver(IdentifierDTO receiver) {
        this.receiver = receiver;
    }

    public void setSender(IdentifierDTO sender) {
        this.sender = sender;
    }

    public void setRoute(IdentifierDTO route) {
        this.route = route;
    }

    public RoutingRule create() {
        Identifier receiver = null;
        if (this.receiver != null) {
            receiver = this.receiver.create();
        }

        Identifier sender = null;
        if (this.sender != null) {
            sender = this.sender.create();
        }

        Identifier route = null;
        if (this.route != null) {
            route = this.route.create();
        }

        return new RoutingRule(type, subtype, receiver, sender, route);
    }

    @Override
    public String toString() {
        return "RoutingRuleDTO{" +
                "type='" + type + '\'' +
                ", subtype='" + subtype + '\'' +
                ", receiver='" + receiver + '\'' +
                ", sender='" + sender + '\'' +
                ", route='" + route + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoutingRuleDTO that = (RoutingRuleDTO) o;

        if (sender != null ? !sender.equals(that.receiver) : that.sender != null) return false;
        if (receiver != null ? !receiver.equals(that.receiver) : that.receiver != null) return false;
        if (route != null ? !route.equals(that.route) : that.route != null) return false;
        if (subtype != null ? !subtype.equals(that.subtype) : that.subtype != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (subtype != null ? subtype.hashCode() : 0);
        result = 31 * result + (receiver != null ? receiver.hashCode() : 0);
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + (route != null ? route.hashCode() : 0);
        return result;
    }


}
