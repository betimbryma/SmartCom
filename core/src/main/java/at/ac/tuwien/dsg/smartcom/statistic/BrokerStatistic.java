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
public class BrokerStatistic {
    private int inputMessageReceived;
    private int inputMessagePublished;
    private int logMessageReceived;
    private int logMessagePublished;
    private int controlMessageReceived;
    private int controlMessagePublished;
    private int outputMessageReceived;
    private int outputMessagePublished;
    private int requestMessageReceived;
    private int requestMessagePublished;

    public BrokerStatistic() {}

    public BrokerStatistic(int inputMessageReceived, int inputMessagePublished,
                           int logMessageReceived, int logMessagePublished,
                           int controlMessageReceived, int controlMessagePublished,
                           int outputMessageReceived, int outputMessagePublished,
                           int requestMessageReceived, int requestMessagePublished) {
        this.inputMessageReceived = inputMessageReceived;
        this.inputMessagePublished = inputMessagePublished;
        this.logMessageReceived = logMessageReceived;
        this.logMessagePublished = logMessagePublished;
        this.controlMessageReceived = controlMessageReceived;
        this.controlMessagePublished = controlMessagePublished;
        this.outputMessageReceived = outputMessageReceived;
        this.outputMessagePublished = outputMessagePublished;
        this.requestMessageReceived = requestMessageReceived;
        this.requestMessagePublished = requestMessagePublished;
    }

    public int getInputMessageReceived() {
        return inputMessageReceived;
    }

    public void setInputMessageReceived(int inputMessageReceived) {
        this.inputMessageReceived = inputMessageReceived;
    }

    public int getInputMessagePublished() {
        return inputMessagePublished;
    }

    public void setInputMessagePublished(int inputMessagePublished) {
        this.inputMessagePublished = inputMessagePublished;
    }

    public int getLogMessageReceived() {
        return logMessageReceived;
    }

    public void setLogMessageReceived(int logMessageReceived) {
        this.logMessageReceived = logMessageReceived;
    }

    public int getLogMessagePublished() {
        return logMessagePublished;
    }

    public void setLogMessagePublished(int logMessagePublished) {
        this.logMessagePublished = logMessagePublished;
    }

    public int getControlMessageReceived() {
        return controlMessageReceived;
    }

    public void setControlMessageReceived(int controlMessageReceived) {
        this.controlMessageReceived = controlMessageReceived;
    }

    public int getControlMessagePublished() {
        return controlMessagePublished;
    }

    public void setControlMessagePublished(int controlMessagePublished) {
        this.controlMessagePublished = controlMessagePublished;
    }

    public int getOutputMessageReceived() {
        return outputMessageReceived;
    }

    public void setOutputMessageReceived(int outputMessageReceived) {
        this.outputMessageReceived = outputMessageReceived;
    }

    public int getOutputMessagePublished() {
        return outputMessagePublished;
    }

    public void setOutputMessagePublished(int outputMessagePublished) {
        this.outputMessagePublished = outputMessagePublished;
    }

    public int getRequestMessageReceived() {
        return requestMessageReceived;
    }

    public void setRequestMessageReceived(int requestMessageReceived) {
        this.requestMessageReceived = requestMessageReceived;
    }

    public int getRequestMessagePublished() {
        return requestMessagePublished;
    }

    public void setRequestMessagePublished(int requestMessagePublished) {
        this.requestMessagePublished = requestMessagePublished;
    }

    @Override
    public String toString() {
        return "BrokerStatistic{" +
                "input: " + inputMessageReceived + "/" + inputMessagePublished +
                ", output: " + outputMessageReceived + "/" + outputMessagePublished +
                ", control: " + controlMessageReceived + "/" + controlMessagePublished +
                ", request: " + requestMessageReceived + "/" + requestMessagePublished +
                ", log: " + logMessageReceived + "/" + logMessagePublished +
                '}';
    }
}
