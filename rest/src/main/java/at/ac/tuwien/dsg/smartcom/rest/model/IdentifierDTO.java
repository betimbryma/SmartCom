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
import at.ac.tuwien.dsg.smartcom.model.IdentifierType;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class IdentifierDTO {

    private String type;
    private String id;
    private String postfix; //for stateful output adapters

    /**
     * @deprecated Should only be used by frameworks that require a default constructor
     */
    public IdentifierDTO() {
    }

    public IdentifierDTO(Identifier id) {
        this.type = id.getType().name();
        this.id = id.returnIdWithoutPostfix();
        this.postfix = id.getPostfix();
    }

    public String getType() {
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

    public void setType(String type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }

    public Identifier create() {
        IdentifierType type = IdentifierType.PEER;

        try {
            type = IdentifierType.valueOf(this.type);
        } catch (IllegalArgumentException ignored) {}

        return new Identifier(type, id, postfix);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdentifierDTO that = (IdentifierDTO) o;

        if (!id.equals(that.id)) return false;
        if (!postfix.equals(that.postfix)) return false;
        if (type.equals(that.type)) return false;

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
        return "IdentifierDTO{" +
                "type=" + type +
                ", id='" + id + '\'' +
                ", postfix='" + postfix + '\'' +
                '}';
    }

}
