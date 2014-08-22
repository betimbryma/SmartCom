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

/**
 * Defines an identifier that distinguishes between different types
 * and id combinations
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class Identifier implements Serializable {

    private final IdentifierType type;
    private final String id;
    private final String postfix; //for stateful output adapters

    /**
     * @deprecated Should only be used by frameworks that require a default constructor
     */
    public Identifier() {
        type = null;
        id = null;
        postfix = null;
    }

    private Identifier(IdentifierType type, String id) {
        this(type, id, "");
    }

    public Identifier(IdentifierType type, String id, String postfix) {
        this.postfix = postfix;
        this.id = id;
        this.type = type;
    }

    public IdentifierType getType() {
        return type;
    }

    public String getId() {
        return id+(postfix.isEmpty() ? "" : "."+postfix);
    }

    public String returnIdWithoutPostfix() {
        return id;
    }

    public String getPostfix() {
        return postfix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Identifier that = (Identifier) o;

        if (!id.equals(that.id)) return false;
        if (!postfix.equals(that.postfix)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + postfix.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Identifier{" +
                "type=" + type +
                ", id='" + id + '\'' +
                ", postfix='" + postfix + '\'' +
                '}';
    }

    public static Identifier adapter(String id) {
        return new Identifier(IdentifierType.ADAPTER, id);
    }

    public static Identifier adapter(String id, String postfix) {
        return new Identifier(IdentifierType.ADAPTER, id, postfix);
    }

    public static Identifier adapter(Identifier id, Identifier postfix) {
        return new Identifier(IdentifierType.ADAPTER, id.id, postfix.id);
    }

    public static Identifier peer(String id) {
        return new Identifier(IdentifierType.PEER, id);
    }

    public static Identifier collective(String id) {
        return new Identifier(IdentifierType.COLLECTIVE, id);
    }

    public static Identifier component(String id) {
        return new Identifier(IdentifierType.COMPONENT, id);
    }

    public static Identifier routing(String id) {
        return new Identifier(IdentifierType.ROUTING_RULE, id);
    }

    public static Identifier message(String id) {
        return new Identifier(IdentifierType.MESSAGE, id);
    }

    public static Identifier channelType(String id) {
        return new Identifier(IdentifierType.CHANNEL, id);
    }
}
