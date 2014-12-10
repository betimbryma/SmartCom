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
package at.ac.tuwien.dsg.smartcom.services.dao;

import at.ac.tuwien.dsg.smartcom.model.MessageInformation;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessageInfoDAO {

    /**
     * Insert new message information.
     * @param information the message information
     */
    void insert(MessageInformation information);

    /**
     * Find a message information identified by a message as the key.
     *
     * It will return either the corresponding message information or null if there is no such
     * message information available.
     *
     * @param key key of the message information
     * @return the message information or null if there is no such message information
     */
    MessageInformation find(MessageInformation.Key key);
}
