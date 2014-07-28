package at.ac.tuwien.dsg.smartcom.model;

import java.io.Serializable;

/**
 * Defines the type of an identifier.
 * Can be either a peer, a collective or an adapter
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public enum IdentifierType implements Serializable{
    PEER, COLLECTIVE, ADAPTER, COMPONENT, ROUTING_RULE, MESSAGE
}
