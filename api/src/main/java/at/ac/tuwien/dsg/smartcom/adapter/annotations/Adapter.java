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
package at.ac.tuwien.dsg.smartcom.adapter.annotations;

import java.lang.annotation.*;

/**
 * This annotation will be used to indicate the properties
 * of an adapter that has been implemented.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Documented
@Retention(value= RetentionPolicy.RUNTIME)
@Target(value= ElementType.TYPE)
public @interface Adapter {

    /**
     * Indicates the name of the adapter (should be unique but can't be guaranteed)
     * @return the name of the adapter
     */
    String name();

    /**
     * Indicates if the adapter is stateful or stateless (default)
     * @return true if adapter is stateful
     */
    boolean stateful() default false;
}
