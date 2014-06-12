package at.ac.tuwien.dsg.smartcom.scm.manager.am;

import at.ac.tuwien.dsg.smartcom.adapter.*;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;
import at.ac.tuwien.dsg.smartcom.scm.manager.am.adapter.FeedbackAdapterFacade;
import at.ac.tuwien.dsg.smartcom.scm.manager.am.adapter.FeedbackPullAdapterFacade;
import at.ac.tuwien.dsg.smartcom.scm.manager.am.adapter.FeedbackPushAdapterFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class AdapterManagerImpl implements AdapterManager {
    private static final Logger log = LoggerFactory.getLogger(AdapterManager.class);

    private final AdapterExecutionEngine executionEngine;
    private final PMCallback peerManager;
    private final MessageBroker broker;
    private final AddressResolver addressResolver;

    private final Map<String, Class<? extends PeerAdapter>> statefulAdapters = new HashMap<>();
    private final Map<String, List<String>> statefulInstances = new HashMap<>();

    private final List<String> stateless = new ArrayList<>();

    public AdapterManagerImpl(PMCallback peerManager, MessageBroker broker) {
        this.peerManager = peerManager;
        this.broker = broker;
        addressResolver = new AddressResolver();
        executionEngine = new AdapterExecutionEngine(addressResolver, broker);
        executionEngine.init();
    }

    @Override
    public void init() {
        executionEngine.init();
    }

    @Override
    public void destroy() {
        executionEngine.destroy();
    }

    @Override
    public String addPushAdapter(FeedbackPushAdapter adapter) {
        String id = generateAdapterId(adapter);

        //init the adapter
        adapter.init();
        if (adapter instanceof FeedbackPushAdapterImpl) {
            ((FeedbackPushAdapterImpl) adapter).setFeedbackPublisher(broker);

        }

        FeedbackAdapterFacade facade = new FeedbackPushAdapterFacade(adapter);
        executionEngine.addFeedbackAdapter(facade, id);

        return id;
    }

    @Override
    public String addPullAdapter(FeedbackPullAdapter adapter) {
        String id = generateAdapterId(adapter);


        FeedbackAdapterFacade facade = new FeedbackPullAdapterFacade(adapter);
        executionEngine.addFeedbackAdapter(facade, id);

        return id;
    }

    @Override
    public FeedbackAdapter removeAdapter(String adapterId) {
        return executionEngine.removeFeedbackAdapter(adapterId);
    }

    @Override
    public String registerPeerAdapter(Class<? extends PeerAdapter> adapter) {
        Adapter annotation = adapter.getAnnotation(Adapter.class);
        boolean stateful = annotation.stateful();
        String name = annotation.name();

        String id = generateAdapterId(adapter, name);

        if (!stateful) {
            try {
                PeerAdapter instance = instantiateClass(adapter);
                executionEngine.addPeerAdapter(instance, id, false);
                stateless.add(id);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("Could not instantiate class "+adapter.toString(), e);
            }
        } else {
            statefulAdapters.put(id, adapter);
        }

        return id;
    }

    private PeerAdapter instantiateClass(Class<? extends PeerAdapter> adapter) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        for (Constructor<?> declaredConstructor : adapter.getDeclaredConstructors()) {
            if (declaredConstructor.getParameterTypes().length == 0) {
                return (PeerAdapter) declaredConstructor.newInstance();
            }
        }

        return null;
    }

    public RoutingRule createEndpointForPeer(String peerId) {
        String adapterId = "";
        Collection<PeerAddress> peerAddress;
        try {
            peerAddress = peerManager.getPeerAddress(peerId);
        } catch (NoSuchPeerException e) {
            log.warn("No such peer: '"+peerId);
            return null;
        }
        for (PeerAddress address : peerAddress) {
            if(stateless.contains("adapter."+address.getAdapter())) {
                addressResolver.addPeerAddress(address);
                adapterId = "adapter."+address.getAdapter();
                break;
            } else if (statefulAdapters.containsKey("adapter."+address.getAdapter())) {
                try {
                    adapterId = createStatefulAdapter(peerId, "adapter."+address.getAdapter());
                    addressResolver.addPeerAddress(address);
                } catch (IllegalAccessException | InstantiationException e) {
                    log.error("Could not instantiate class " + statefulAdapters.get("adapter."+address.getAdapter()).toString(), e);
                }
                break;
            } else {
                log.warn("Unknown adapter: "+address.getAdapter());
            }
        }

        if ("".equals(adapterId)) {
            return null;
        }

        return new RoutingRule("", "", peerId, adapterId);
    }

    private String createStatefulAdapter(String peerId, String adapterId) throws IllegalAccessException, InstantiationException {
        Class<? extends PeerAdapter> peerAdapterClass = statefulAdapters.get(adapterId);
        PeerAdapter peerAdapter = peerAdapterClass.newInstance();
        String id = adapterId+"."+peerId;
        executionEngine.addPeerAdapter(peerAdapter, id, true);

        List<String> instances = statefulInstances.get(adapterId);
        if (instances == null) {
            instances = new ArrayList<>();
            statefulInstances.put(adapterId, instances);
        }
        instances.add(id);
        return id;
    }

    @Override
    public void removePeerAdapter(String adapterId) {
        stateless.remove(adapterId);
        Class<? extends PeerAdapter> remove = statefulAdapters.remove(adapterId);
        if (remove != null) {
            for (String id : statefulInstances.get(adapterId)) {
                executionEngine.removePeerAdapter(id);
            }
        }
        executionEngine.removePeerAdapter(adapterId);
    }

    private String generateAdapterId(FeedbackAdapter adapter) {
        return "adapter."+generateUniqueIdString();
    }

    private String generateAdapterId(Class<? extends PeerAdapter> adapter, String name) {
        return "adapter."+name;
    }

    private String generateUniqueIdString() {
        return UUID.randomUUID().toString();
    }
}
