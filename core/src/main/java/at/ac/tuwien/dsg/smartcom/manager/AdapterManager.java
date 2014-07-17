package at.ac.tuwien.dsg.smartcom.manager;

import at.ac.tuwien.dsg.smartcom.adapter.InputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.model.Identifier;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface AdapterManager {

    /**
     * Initializes the adapter manager
     */
    void init();

    /**
     * Destroys the adapter manager and cleans up resources
     */
    void destroy();

    /**
     * Add a push adapter to the adapter manager
     * @param adapter the push adapter
     * @return the id of the push adapter
     */
    Identifier addPushAdapter(InputPushAdapter adapter);

    /**
     * Add a pull adapter to the adapter manager.
     * @param adapter the pull adapter
     * @param period defines the period between two pull attempts
     * @return the id of the pull adapter
     */
    Identifier addPullAdapter(InputPullAdapter adapter, int period);

    /**
     * Removes a input adapter from the execution. Note that after returning,
     * the corresponding input adapter won't return any input anymore.
     * @param adapterId the id of the adapter
     * @return the adapter that has been removed or null if there is no such adapter
     */
    InputAdapter removeInputAdapter(Identifier adapterId);

    /**
     * Register a new type of output adapters in the adapter manager.
     * @param adapter new type of output adapters
     * @return id for the output adapter type
     */
    Identifier registerOutputAdapter(Class<? extends OutputAdapter> adapter);

    /**
     * Removes a output adapter type and all instances from the execution. After the method
     * returned no adapter of this type will handle messages anymore.
     * @param adapterId id of the output adapter type
     */
    void removeOutputAdapter(Identifier adapterId);

    /**
     * Create a new endpoint (adapter) for a specific peer. If there is already an
     * endpoint, a corresponding routing rule will be returned. If there is no such
     * adapter available, a new one will be created (based on available contact information
     * of the peer) and a corresponding routing rule will be returned.
     * @param peerId id of the peer that requires a new endpoint
     * @return the identifier of the new endpoint for the peer
     */
    public Identifier createEndpointForPeer(Identifier peerId);
}
