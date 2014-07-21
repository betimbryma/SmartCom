package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.adapter.InputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.exception.InvalidRuleException;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.MessagingAndRoutingManager;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;
import org.picocontainer.annotations.Inject;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class CommunicationImpl implements Communication {

    @Inject
    private MessagingAndRoutingManager marManager;

    @Inject
    private AdapterManager adapterManager;

    @Override
    public Identifier send(Message message) throws CommunicationException {
        return marManager.send(message);
    }

    @Override
    public Identifier addRouting(RoutingRule rule) throws InvalidRuleException {
        return marManager.addRouting(rule);
    }

    @Override
    public RoutingRule removeRouting(Identifier routeId) {
        return marManager.removeRouting(routeId);
    }

    @Override
    public Identifier addPushAdapter(InputPushAdapter adapter) {
        return adapterManager.addPushAdapter(adapter);
    }

    @Override
    public Identifier addPullAdapter(InputPullAdapter adapter, long interval) {
        return adapterManager.addPullAdapter(adapter, interval);
    }

    @Override
    public InputAdapter removeInputAdapter(Identifier adapterId) {
        return adapterManager.removeInputAdapter(adapterId);
    }

    @Override
    public Identifier registerOutputAdapter(Class<OutputAdapter> adapter) {
        return adapterManager.registerOutputAdapter(adapter);
    }

    @Override
    public void removeOutputAdapter(Identifier adapterId) {
        adapterManager.removeOutputAdapter(adapterId);
    }

    @Override
    public void registerNotificationCallback(NotificationCallback callback) {
        marManager.registerNotificationCallback(callback);
    }
}
