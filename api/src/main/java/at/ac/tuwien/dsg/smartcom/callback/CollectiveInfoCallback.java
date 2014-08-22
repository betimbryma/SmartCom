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
package at.ac.tuwien.dsg.smartcom.callback;

import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.model.CollectiveInfo;
import at.ac.tuwien.dsg.smartcom.model.Identifier;

/**
 * This API is used to provide different information regarding the composition
 * and the state of the collectives to the Middleware, in order for the Middleware
 * to allow to other SmartSociety components the functionality of addressing their
 * messages on the Collective level.
 *
 * At this point, the API consists of a single method, but as the TEE and EPE components
 * get developed later, the API may grow and/or change.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface CollectiveInfoCallback {

    /**
     * Resolves and returns the members of a given collective id.
     *
     * @param collective The id of the collective
     * @return Returns a list of peer ids that are part of the collective and other collective related information
     * @throws NoSuchCollectiveException if there exists no such collective.
     */
    public CollectiveInfo getCollectiveInfo(Identifier collective) throws NoSuchCollectiveException;
}
