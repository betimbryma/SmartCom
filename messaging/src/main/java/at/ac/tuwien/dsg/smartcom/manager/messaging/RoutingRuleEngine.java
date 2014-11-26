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
package at.ac.tuwien.dsg.smartcom.manager.messaging;

import at.ac.tuwien.dsg.smartcom.exception.InvalidRuleException;
import at.ac.tuwien.dsg.smartcom.manager.messaging.util.KeyProvider;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class RoutingRuleEngine {

    protected final Map<String, Map<String, Map<Identifier, Map<Identifier, Identifier>>>> routing =
            Collections.synchronizedMap(new HashMap<String, Map<String, Map<Identifier, Map<Identifier, Identifier>>>>());

    private final Map<Identifier, RoutingRule> rules = new ConcurrentHashMap<>();

    public Identifier addRouting(RoutingRule rule) throws InvalidRuleException {

        validateRule(rule);

        Identifier id = Identifier.routing(KeyProvider.generateUniqueIdString());

        Map<String, Map<Identifier, Map<Identifier, Identifier>>> step1 = routing.get(rule.getType());
        if (step1 == null) {
            synchronized (routing) {
                step1 = routing.get(rule.getType());
                if (step1 == null) {
                    step1 = Collections.synchronizedMap(new HashMap<String, Map<Identifier, Map<Identifier, Identifier>>>());
                    routing.put(rule.getType(), step1);
                }
            }
        }

        Map<Identifier, Map<Identifier, Identifier>> step2 = step1.get(rule.getSubtype());
        if (step2 == null) {
            synchronized (step1) {
                step2 = step1.get(rule.getSubtype());
                if (step2 == null) {
                    step2 = Collections.synchronizedMap(new HashMap<Identifier, Map<Identifier, Identifier>>());
                    step1.put(rule.getSubtype(), step2);
                }
            }
        }

        Map<Identifier, Identifier> step3 = step2.get(rule.getReceiver());
        if (step3 == null) {
            synchronized (step2) {
                step3 = step2.get(rule.getReceiver());
                if (step3 == null) {
                    step3 = Collections.synchronizedMap(new HashMap<Identifier, Identifier>());
                    step2.put(rule.getReceiver(), step3);
                }
            }
        }
        step3.put(id, rule.getRoute());

        rules.put(id, rule);

        return id;
    }

    private static void validateRule(RoutingRule rule) throws InvalidRuleException {
        if (rule.getRoute() == null) {
            throw new InvalidRuleException("Endpoint of the routing rule is not defined!");
        }

        if (rule.getType() == null || rule.getType().isEmpty()) {
            if (rule.getSubtype() == null || rule.getSubtype().isEmpty()) {
                if (rule.getReceiver() == null || rule.getReceiver().getId() == null || rule.getReceiver().getId().isEmpty()) {
                    throw new InvalidRuleException("Type, Subtype, and Receiver of the routing rule are not defined!");
                }
            }
        }
    }

    public Collection<Identifier> performRouting(Message message) {
        String type = (message.getType() == null || message.getType().isEmpty()? null : message.getType());
        String subtype = (message.getSubtype() == null || message.getSubtype().isEmpty()? null : message.getSubtype());
        Identifier receiver = (message.getReceiverId() == null || message.getReceiverId().getId() == null || message.getReceiverId().getId().isEmpty() ? null : message.getReceiverId());

        return handleStep1(type, subtype, receiver);
    }

    private Collection<Identifier> handleStep1(String type, String subtype, Identifier receiver) {
        Set<Identifier> list = new HashSet<>();

        list.addAll(handleStep2(routing.get(type), subtype, receiver));
        if (type != null) {
            list.addAll(handleStep2(routing.get(null), subtype, receiver));
        }

        return list;
    }

    private Collection<Identifier> handleStep2(Map<String, Map<Identifier, Map<Identifier, Identifier>>> step1, String subtype, Identifier receiver) {
        if (step1 == null) {
            return Collections.emptyList();
        }

        Set<Identifier> list = new HashSet<>();

        list.addAll(handleStep3(step1.get(subtype), receiver));
        if (subtype != null) {
            list.addAll(handleStep3(step1.get(null), receiver));
        }

        return list;
    }

    private Collection<Identifier> handleStep3(Map<Identifier, Map<Identifier, Identifier>> step2, Identifier receiver) {
        if (step2 == null) {
            return Collections.emptyList();
        }

        Set<Identifier> list = new HashSet<>();

        list.addAll(handleStep4(step2.get(receiver)));
        if (receiver != null) {
            list.addAll(handleStep4(step2.get(null)));
        }

        return list;
    }

    private Collection<Identifier> handleStep4(Map<Identifier, Identifier> step3) {
        if (step3 != null) {
            return step3.values();
        }
        return Collections.emptyList();
    }

    public RoutingRule removeRouting(Identifier routeId) {
        RoutingRule rule = rules.remove(routeId);

        if (rule != null) {
            synchronized (routing) {
                Map<String, Map<Identifier, Map<Identifier, Identifier>>> step1 = routing.get(rule.getType());
                if (step1 != null) {
                    Map<Identifier, Map<Identifier, Identifier>> step2 = step1.get(rule.getSubtype());
                    if (step2 != null) {
                        Map<Identifier, Identifier> step3 = step2.get(rule.getReceiver());
                        if (step3 != null) {
                            step3.remove(routeId);

                            //clean up if they are empty
                            if (step3.size() == 0) {
                                step2.remove(rule.getReceiver());
                                if (step2.size() == 0) {
                                    step1.remove(rule.getSubtype());
                                    if (step1.size() == 0) {
                                        routing.remove(rule.getType());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return rule;
    }

    public void clear() {
        rules.clear();
    }
}
