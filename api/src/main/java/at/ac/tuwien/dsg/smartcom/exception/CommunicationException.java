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
package at.ac.tuwien.dsg.smartcom.exception;


import java.util.Map;
import java.util.TreeMap;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class CommunicationException extends Exception {
    private ErrorCode errorCode;
    private Map<String, Object> properties = new TreeMap<>();

    public static CommunicationException wrap(Throwable exception, ErrorCode errorCode) {
        if (exception instanceof CommunicationException) {
            CommunicationException se = (CommunicationException)exception;
            if (errorCode != null && errorCode != se.getErrorCode()) {
                return new CommunicationException(exception.getMessage(), exception, errorCode);
            }
            return se;
        } else {
            return new CommunicationException(exception.getMessage(), exception, errorCode);
        }
    }

    public static CommunicationException wrap(Throwable exception) {
        return wrap(exception, null);
    }

    public CommunicationException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public CommunicationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public CommunicationException(Throwable cause, ErrorCode errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public CommunicationException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public CommunicationException setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Object get(String name) {
        return properties.get(name);
    }

    public CommunicationException set(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return "CommunicationException{" +
                "errorCode=" + errorCode +
                '}';
    }
}
