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
package at.ac.tuwien.dsg.smartcom.manager.am.util;

import at.ac.tuwien.dsg.smartcom.utils.ExpiringCounter;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ExpiringCounterTest {

    @Test
    public void testIncrease() throws Exception {
        ExpiringCounter counter = new ExpiringCounter(5, TimeUnit.SECONDS);
        counter.increase();
        counter.increase();
        counter.increase();
        counter.increase();
        counter.increase();

        synchronized (this) {
            wait(6000);
        }

        assertEquals(0, counter.getCounter());
    }
}