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
package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.model.QueryCriteria;
import at.ac.tuwien.dsg.smartcom.services.MessageQueryService;
import at.ac.tuwien.dsg.smartcom.services.MessageQueryServiceImpl;
import at.ac.tuwien.dsg.smartcom.services.dao.MongoDBMessageQueryDAO;
import at.ac.tuwien.dsg.smartcom.utils.MessageQueryTestClass;
import at.ac.tuwien.dsg.smartcom.utils.PicoHelper;
import org.junit.Before;

public class MessageQueryServiceImplIT extends MessageQueryTestClass {

    private PicoHelper pico;
    private MessageQueryService service;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        pico = new PicoHelper();
        pico.addComponent(logger);
        pico.addComponent(new MongoDBMessageQueryDAO(mongoDB.getClient(), "test-log", "log"));
        pico.addComponent(MessageQueryServiceImpl.class);

        service = pico.getComponent(MessageQueryService.class);

        pico.start();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        pico.stop();
    }

    @Override
    public QueryCriteria createCriteria() {
        return service.createQuery();
    }
}