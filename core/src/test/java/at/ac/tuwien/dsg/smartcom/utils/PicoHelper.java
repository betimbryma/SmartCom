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
package at.ac.tuwien.dsg.smartcom.utils;

import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PicoHelper {

    private final MutablePicoContainer pico;

    public PicoHelper() {
        pico = new PicoBuilder().withAnnotatedFieldInjection().withJavaEE5Lifecycle().withCaching().build();
    }

    public void start() {
        pico.start();
    }

    public void stop() {
        pico.stop();
        pico.dispose();
    }

    public void addComponent(Object component) {
        pico.as(Characteristics.CACHE).addComponent(component);
    }

    public void addComponent(Object name, Object instance) {
        pico.as(Characteristics.CACHE).addComponent(name, instance);
    }

    public <T> T getComponent(Class<T> componentType) {
        return pico.getComponent(componentType);
    }
}
