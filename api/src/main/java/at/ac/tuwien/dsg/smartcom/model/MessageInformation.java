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
 * TODO add description
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MessageInformation {

    private Key key;
    private String purpose;
    private String validAnswer;
    private List<Key> validAnswerTypes;
    private List<Key> relatedMessages;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getValidAnswer() {
        return validAnswer;
    }

    public void setValidAnswer(String validAnswer) {
        this.validAnswer = validAnswer;
    }

    public List<Key> getValidAnswerTypes() {
        return validAnswerTypes;
    }

    public void setValidAnswerTypes(List<Key> validAnswerTypes) {
        this.validAnswerTypes = validAnswerTypes;
    }

    public List<Key> getRelatedMessages() {
        return relatedMessages;
    }

    public void setRelatedMessages(List<Key> relatedMessages) {
        this.relatedMessages = relatedMessages;
    }

    public static class Key {
        private String type;
        private String subtype;

        Key() {
            this.type = null;
            this.subtype = null;
        }

        public Key(String type, String subtype) {
            this.type = type;
            this.subtype = subtype;
        }

        public String getType() {
            return type;
        }

        public String getSubtype() {
            return subtype;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (subtype != null ? !subtype.equals(key.subtype) : key.subtype != null) return false;
            if (type != null ? !type.equals(key.type) : key.type != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (subtype != null ? subtype.hashCode() : 0);
            return result;
        }
    }

    @Override
    public String toString() {
        return "MessageInformation{" +
                "key=" + key +
                ", purpose='" + purpose + '\'' +
                ", validAnswer='" + validAnswer + '\'' +
                ", validAnswerTypes=" + validAnswerTypes +
                ", relatedMessages=" + relatedMessages +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageInformation that = (MessageInformation) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (purpose != null ? !purpose.equals(that.purpose) : that.purpose != null) return false;
        if (relatedMessages != null ? !relatedMessages.equals(that.relatedMessages) : that.relatedMessages != null)
            return false;
        if (validAnswer != null ? !validAnswer.equals(that.validAnswer) : that.validAnswer != null) return false;
        if (validAnswerTypes != null ? !validAnswerTypes.equals(that.validAnswerTypes) : that.validAnswerTypes != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (purpose != null ? purpose.hashCode() : 0);
        result = 31 * result + (validAnswer != null ? validAnswer.hashCode() : 0);
        result = 31 * result + (validAnswerTypes != null ? validAnswerTypes.hashCode() : 0);
        result = 31 * result + (relatedMessages != null ? relatedMessages.hashCode() : 0);
        return result;
    }
}
