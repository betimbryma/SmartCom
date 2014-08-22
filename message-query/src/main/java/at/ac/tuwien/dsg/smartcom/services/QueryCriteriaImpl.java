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

import at.ac.tuwien.dsg.smartcom.exception.IllegalQueryException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.QueryCriteria;
import at.ac.tuwien.dsg.smartcom.services.dao.MessageQueryDAO;

import java.util.Collection;
import java.util.Date;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class QueryCriteriaImpl implements QueryCriteria {
    private final MessageQueryDAO dao;

    private Message.MessageBuilder builder;
    private Date from;
    private Date to;

    public QueryCriteriaImpl(MessageQueryDAO dao) {
        this.dao = dao;
        this.builder = new Message.MessageBuilder();
    }

    @Override
    public Collection<Message> query() throws IllegalQueryException {
        if (from != null && to != null) {
            if (from.compareTo(to) > 0) {
                throw  new IllegalQueryException("Parameter 'to' is before 'from'");
            }
        }

        return dao.query(this);
    }

    @Override
    public QueryCriteria from(Identifier id) {
        builder.setSenderId(id);
        return this;
    }

    @Override
    public QueryCriteria to(Identifier id) {
        builder.setReceiverId(id);
        return this;
    }

    @Override
    public QueryCriteria id(Identifier id) {
        builder.setId(id);
        return this;
    }

    @Override
    public QueryCriteria conversationId(String id) {
        builder.setConversationId(id);
        return this;
    }

    @Override
    public QueryCriteria type(String type) {
        builder.setType(type);
        return this;
    }

    @Override
    public QueryCriteria subtype(String subtype) {
        builder.setSubtype(subtype);
        return this;
    }

    @Override
    public QueryCriteria created(Date from, Date to) {
        this.from = from;
        this.to = to;
        return this;
    }

    public Message.MessageBuilder getBuilder() {
        return builder;
    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }
}
