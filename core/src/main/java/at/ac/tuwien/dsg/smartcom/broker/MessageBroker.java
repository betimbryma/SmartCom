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
package at.ac.tuwien.dsg.smartcom.broker;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 * A broker that will be used to send messages using a queue or a
 * similar mechanism. Will only be used internally.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessageBroker extends InputPublisher {

    /**
     * Receives input from the underlying communication channel.
     * Might block until there is input available.
     *
     * @return input
     */
    public Message receiveInput();

    /**
     * Register a message listener that will be notified on the arrival of
     * new input messages. Note that a message listener will consume the message
     * and that there can only be one listener at a time.
     *
     * @param listener for input messages
     * @return listener that can be cancelled
     */
    public CancelableListener registerInputListener(MessageListener listener);

    /**
     * Receives request messages for a given id. Might block until there
     * is a message available.
     *
     * @param id that the request message is assigned to
     * @return the message
     */
    public Message receiveRequests(Identifier id);

    /**
     * Register a message listener that will be notified on the arrival of
     * new request messages for a given receiver. Note that a message listener
     * will consume the message and that there can only be one listener at a
     * time for each id.
     * @param id the id of the request receiver
     * @param listener for request messages
     * @return listener that can be cancelled
     */
    public CancelableListener registerRequestListener(Identifier id, MessageListener listener);

    /**
     * Publish a new request message for a given id.
     *
     * @param id that the messages is dedicated to
     * @param message that should be published
     */
    public void publishRequest(Identifier id, Message message);

    /**
     * Receives output messages for a given id. Might block until there
     * is a message available.
     *
     * @param id that the task message is assigned to
     * @return the message
     */
    public Message receiveOutput(Identifier id);

    /**
     * Register a message listener that will be notified on the arrival of
     * new output messages for a given receiver. Note that a message listener
     * will consume the message and that there can only be one listener at a
     * time for each id.
     *
     * @param id the id of the task receiver
     * @param listener for task messages
     * @return listener that can be cancelled
     */
    public CancelableListener registerOutputListener(Identifier id, MessageListener listener);

    /**
     * Publish an output message for a given receiver destination id (note that
     * this is not the same as the receiver id! One receiver id can be resolved
     * to many receiver destination ids).
     *
     * @param id of the receiver destination
     * @param message that should be published
     */
    public void publishOutput(Identifier id, Message message);

    /**
     * Publish a control message.
     *
     * @param message control message
     */
    public void publishControl(Message message);

    /**
     * Receives control messages. Might block until there
     * is a message available.
     *
     * @return the message
     */
    public Message receiveControl();

    /**
     * Register a message listener that will be notified on the arrival of
     * new control messages. Note that a message listener will consume the message
     * and that there can only be one listener at a time.
     *
     * @param listener for control messages
     * @return listener that can be cancelled
     */
    public CancelableListener registerControlListener(MessageListener listener);

    /**
     * Publish authentication request message.
     *
     * @param message that should be published
     */
    public void publishAuthRequest(Message message);

    /**
     * Receives authentication messages. Might block until there
     * is a message available.
     *
     * @return the message
     */
    public Message receiveAuthRequest();

    /**
     * Register a message listener that will be notified on the arrival of
     * new authentication messages. Note that a message listener will consume the message
     * and that there can only be one listener at a time.
     *
     * @param listener for authentication messages
     * @return listener that can be cancelled
     */
    public CancelableListener registerAuthListener(MessageListener listener);

    /**
     * publish message info request message.
     *
     * @param message that should be published
     */
    public void publishMessageInfoRequest(Message message);

    /**
     * Receives message info request messages. Might block until there
     * is a message available.
     *
     * @return the message
     */
    public Message receiveMessageInfoRequest();

    /**
     * Register a message listener that will be notified on the arrival of
     * new message info request messages. Note that a message listener will consume the message
     * and that there can only be one listener at a time.
     *
     * @param listener for message info request messages
     * @return listener that can be cancelled
     */
    public CancelableListener registerMessageInfoListener(MessageListener listener);

    /**
     * publishes a metrics request message.
     *
     * @param message that should be published
     */
    public void publishMetricsRequest(Message message);

    /**
     * Receives metrics request messages. Might block until there
     * is a message available.
     *
     * @return the message
     */
    public Message receiveMetricsRequest();

    /**
     * Register a message listener that will be notified on the arrival of
     * new metrics request messages. Note that a message listener will consume the message
     * and that there can only be one listener at a time.
     *
     * @param listener for metrics request messages
     * @return listener that can be cancelled
     */
    public CancelableListener registerMetricsListener(MessageListener listener);

    /**
     * publishes a log message
     *
     * @param message that should be published
     */
    public void publishLog(Message message);

    /**
     * Receives log messages. Might block until there
     * is a message available.
     *
     * @return the message
     */
    public Message receiveLog();

    /**
     * Register a message listener that will be notified on the arrival of
     * new log messages. Note that a message listener will consume the message
     * and that there can only be one listener at a time.
     *
     * @param listener for log messages
     * @return listener that can be cancelled
     */
    public CancelableListener registerLogListener(MessageListener listener);
}
