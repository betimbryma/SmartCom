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
package at.ac.tuwien.dsg.smartcom.statistic;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class Statistic {
    private int sendingRequest;
    private int internalSendingRequest;
    private int externalSendingRequest;
    private int componentMessage;
    private int collectiveMessage;
    private int peerMessage;
    private int callbackMessage;
    private int logMessage;

    private BrokerStatistic broker;

    public Statistic() {}

    public Statistic(int sendingRequest, int internalSendingRequest,
                     int externalSendingRequest, int componentMessage,
                     int collectiveMessage, int peerMessage,
                     int callbackMessage, int logMessage,
                     BrokerStatistic broker) {
        this.sendingRequest = sendingRequest;
        this.internalSendingRequest = internalSendingRequest;
        this.externalSendingRequest = externalSendingRequest;
        this.componentMessage = componentMessage;
        this.collectiveMessage = collectiveMessage;
        this.peerMessage = peerMessage;
        this.callbackMessage = callbackMessage;
        this.logMessage = logMessage;
        this.broker = broker;
    }

    public BrokerStatistic getBroker() {
        return broker;
    }

    public void setBroker(BrokerStatistic broker) {
        this.broker = broker;
    }

    public int getSendingRequest() {
        return sendingRequest;
    }

    public void setSendingRequest(int sendingRequest) {
        this.sendingRequest = sendingRequest;
    }

    public int getInternalSendingRequest() {
        return internalSendingRequest;
    }

    public void setInternalSendingRequest(int internalSendingRequest) {
        this.internalSendingRequest = internalSendingRequest;
    }

    public int getExternalSendingRequest() {
        return externalSendingRequest;
    }

    public void setExternalSendingRequest(int externalSendingRequest) {
        this.externalSendingRequest = externalSendingRequest;
    }

    public int getComponentMessage() {
        return componentMessage;
    }

    public void setComponentMessage(int componentMessage) {
        this.componentMessage = componentMessage;
    }

    public int getCollectiveMessage() {
        return collectiveMessage;
    }

    public void setCollectiveMessage(int collectiveMessage) {
        this.collectiveMessage = collectiveMessage;
    }

    public int getPeerMessage() {
        return peerMessage;
    }

    public void setPeerMessage(int peerMessage) {
        this.peerMessage = peerMessage;
    }

    public int getCallbackMessage() {
        return callbackMessage;
    }

    public void setCallbackMessage(int callbackMessage) {
        this.callbackMessage = callbackMessage;
    }

    public int getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(int logMessage) {
        this.logMessage = logMessage;
    }

    @Override
    public String toString() {
        return "Statistic{" +
                "sendingRequest=" + sendingRequest +
                ", internalSendingRequest=" + internalSendingRequest +
                ", externalSendingRequest=" + externalSendingRequest +
                ", componentMessage=" + componentMessage +
                ", collectiveMessage=" + collectiveMessage +
                ", peerMessage=" + peerMessage +
                ", callbackMessage=" + callbackMessage +
                ", logMessage=" + logMessage +
                ", broker: "+broker.toString()+
                '}';
    }
}
