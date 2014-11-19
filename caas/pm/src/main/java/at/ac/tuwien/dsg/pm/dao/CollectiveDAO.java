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
package at.ac.tuwien.dsg.pm.dao;

import at.ac.tuwien.dsg.pm.exceptions.CollectiveAlreadyExistsException;
import at.ac.tuwien.dsg.pm.model.Collective;

import java.util.List;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface CollectiveDAO {

    public Collective addCollective(Collective collective) throws CollectiveAlreadyExistsException;

    public Collective getCollective(String id);

    public List<Collective> getAll();

    /**
     * Note that this method does not update the peers of the collective!
     * @param collective
     * @return
     */
    public Collective updateCollective(Collective collective);

    public Collective addPeerToCollective(String collectiveId, String peerId);

    public Collective removePeerToCollective(String collectiveId, String peerId);

    public Collective deleteCollective(String id);

    public void clearData();
}
