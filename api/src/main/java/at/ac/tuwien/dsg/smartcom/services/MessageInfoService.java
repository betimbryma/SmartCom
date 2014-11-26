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
package at.ac.tuwien.dsg.smartcom.services;

import at.ac.tuwien.dsg.smartcom.exception.UnknownMessageException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.MessageInformation;

/**
 * The message info service provides information on the semantics of messages,
 * how to interpret them in a human-readable way and which messages are related
 * to a message. Therefore it provides methods to query message information and
 * to add additional information to messages.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessageInfoService {

    /**
     * Returns information on a given message to the caller. This contains how the message has to
     * be interpreted, how it is related to other messages and which messages are expected in
     * response to this message.
     *
     * @param message Can be a valid message ID or an instance of Message.
     * @return Returns the information for a given message
     * @throws UnknownMessageException no message of that type found or the MessageId is not valid.
     */
    public MessageInformation getInfoForMessage(Message message) throws UnknownMessageException;

    /**
     * Add information on a given message. If there is already exists information for a message,
     * it will be replaced by this one.
     *
     * @param message Specifies the type of message.
     * @param info Information for messages of the type of parameter message.
     */
    public void addMessageInfo(Message message, MessageInformation info);
}
