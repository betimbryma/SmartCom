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
package at.ac.tuwien.dsg.smartcom.adapters.rest;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.*;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {
    private final static String ENTITY_PACKAGE = "at.ac.tuwien.dsg.smartcom.model";
    private final static JAXBContext context;
    static {
//        try {
            context = null; //new JAXBContextAdapter(new JSONJAXBContext(JSONConfiguration.mapped().rootUnwrapping(false).build(), ENTITY_PACKAGE));
//        } catch (final JAXBException ex) {
//            throw new IllegalStateException("Could not resolve JAXBContext.", ex);
//        }
    }

    public JAXBContext getContext(final Class<?> type) {
        try {
            if (type.getPackage().getName().contains(ENTITY_PACKAGE)) {
                return context;
            }
        } catch (final Exception ex) {
            // trap, just return null
        }
        return null;
    }

    public static final class JAXBContextAdapter extends JAXBContext {
        private final JAXBContext context;

        public JAXBContextAdapter(final JAXBContext context) {
            this.context = context;
        }

        @Override
        public Marshaller createMarshaller() {
            Marshaller marshaller = null;
            try {
                marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            } catch (final PropertyException pe) {
                return marshaller;
            } catch (final JAXBException jbe) {
                return null;
            }
            return marshaller;
        }

        @Override
        public Unmarshaller createUnmarshaller() throws JAXBException {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setEventHandler(new DefaultValidationEventHandler());
            return unmarshaller;
        }

        @Override
        public Validator createValidator() throws JAXBException {
            return context.createValidator();
        }
    }
}
