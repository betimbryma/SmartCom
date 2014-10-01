package at.ac.tuwien.dsg.smartcom.broker.policy;

/**
 * Defines the type of the replication result (upscale, downscale, no action) and
 * how many resources should be added/removed.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class ReplicationPolicyResult {
    private final ReplicationType type;
    private final int amount;

    ReplicationPolicyResult(ReplicationType type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public ReplicationType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }
}
