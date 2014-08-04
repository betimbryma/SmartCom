package at.ac.tuwien.dsg.smartcom.model;

import at.ac.tuwien.dsg.smartcom.exception.IllegalQueryException;

import java.util.Collection;
import java.util.Date;

/**
 * The QueryCriteria object can be used to specify the criteria that are used to query for messages
 * in the message log. The query criteria can be specified and the query can be executed by calling the
 * query() method.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface QueryCriteria {

    /**
     * Execute the specified query and get the results of the query.
     * If nothing has been specified, all messages will be returned (can be quite expensive).
     *
     * If the 'to' timestamp to query the creation date/time of the message is before the 'from' timestamp
     * an IllegalQueryException will be thrown.
     *
     * @return Collection of found messages
     * @throws IllegalQueryException if 'to' timestamp is before the 'from timestamp'
     */
    public Collection<Message> query() throws IllegalQueryException;

    /**
     * Specifies the sender of the message.
     * @param id of the sender
     * @return the same QueryCriteria with changed data
     */
    public QueryCriteria from(Identifier id);

    /**
     * Specifies the receiver of the message.
     * @param id of the receiver
     * @return the same QueryCriteria with changed data
     */
    public QueryCriteria to(Identifier id);

    /**
     * Specifies the id of the message.
     * @param id of the message
     * @return the same QueryCriteria with changed data
     */
    public QueryCriteria id(Identifier id);

    /**
     * Specifies the conversation id of the message.
     * @param id of the conversation
     * @return the same QueryCriteria with changed data
     */
    public QueryCriteria conversationId(String id);

    /**
     * Specifies the type of the message.
     * @param type of the message
     * @return the same QueryCriteria with changed data
     */
    public QueryCriteria type(String type);

    /**
     * Specifies the subtype of the message.
     * @param subtype of the message
     * @return the same QueryCriteria with changed data
     */
    public QueryCriteria subtype(String subtype);

    /**
     * Specifies the closed interval ([from, to]) within the message has been created.
     * 'To' has to be the equal or after 'from'.
     *
     * @param from the start of the period
     * @param to the end of the period
     * @return the same QueryCriteria with changed data
     */
    public QueryCriteria created(Date from, Date to);

}
