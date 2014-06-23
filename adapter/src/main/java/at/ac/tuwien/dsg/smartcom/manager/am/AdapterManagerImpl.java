package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.adapter.*;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.FeedbackAdapterFacade;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.FeedbackPullAdapterFacade;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.FeedbackPushAdapterFacade;
import at.ac.tuwien.dsg.smartcom.manager.am.dao.ResolverDAO;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class AdapterManagerImpl implements AdapterManager {
    private static final Logger log = LoggerFactory.getLogger(AdapterManager.class);
    public static final String ADAPTER_PREFIX = "adapter.";

    private final AdapterExecutionEngine executionEngine;
    private final PMCallback peerManager;
    private final MessageBroker broker;
    private final AddressResolver addressResolver;

    private final Map<String, Class<? extends PeerAdapter>> statefulAdapters = new ConcurrentHashMap<>();
    private final Map<String, List<String>> statefulInstances = new ConcurrentHashMap<>();

    private final List<String> stateless = new ArrayList<>();

    AdapterManagerImpl(ResolverDAO dao, PMCallback peerManager, MessageBroker broker) {
        this.peerManager = peerManager;
        this.broker = broker;
        addressResolver = new AddressResolver(dao, 5000); //TODO
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

        if (adapter instanceof FeedbackPushAdapterImpl) {
            ((FeedbackPushAdapterImpl) adapter).setFeedbackPublisher(broker);
        }

        //init the adapter
        adapter.init();

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
    public FeedbackAdapter removeFeedbackAdapter(String adapterId) {
        return executionEngine.removeFeedbackAdapter(adapterId);
    }

    @Override
    public String registerPeerAdapter(Class<? extends PeerAdapter> adapter) {
        Adapter annotation = adapter.getAnnotation(Adapter.class);
        if (annotation == null) {
            log.error("Can't find annotation @Adapter in class ()", adapter.getSimpleName());
            return null;
        }
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

    @Override
    public RoutingRule createEndpointForPeer(String peerId) {
        String adapterId = null;
        Collection<PeerAddress> peerAddress;
        try {
            peerAddress = peerManager.getPeerAddress(peerId);
        } catch (NoSuchPeerException e) {
            log.warn("No such peer: '"+peerId);
            return null;
        }
        for (PeerAddress address : peerAddress) {
            if(stateless.contains(ADAPTER_PREFIX+address.getAdapter())) {
                addressResolver.addPeerAddress(address);
                adapterId = ADAPTER_PREFIX+address.getAdapter();
                break;
            } else if (statefulAdapters.containsKey(ADAPTER_PREFIX+address.getAdapter())) {
                try {
                    String adapter = ADAPTER_PREFIX+address.getAdapter();
                    String newId = adapter+"."+peerId;

                    //check if there is already such an instance
                    synchronized (statefulInstances) {
                        List<String> instances = statefulInstances.get(ADAPTER_PREFIX + address.getAdapter());
                        if (instances != null) {
                            if (instances.contains(newId)) {
                                adapterId = newId;
                                break;
                            }
                        } else {
                            instances = new ArrayList<>();
                            statefulInstances.put(adapter, instances);
                        }

                        Class<? extends PeerAdapter> peerAdapterClass = statefulAdapters.get(adapter);
                        PeerAdapter peerAdapter;
                        try {
                            peerAdapter = instantiateClass(peerAdapterClass);
                        } catch (InvocationTargetException e) {
                            log.error("Could not instantiate class "+adapter, e);
                            continue;
                        }
                        executionEngine.addPeerAdapter(peerAdapter, newId, true);
                        adapterId = newId;
                        instances.add(adapterId);
                    }
                    addressResolver.addPeerAddress(address);
                } catch (IllegalAccessException | InstantiationException e) {
                    log.error("Could not instantiate class " + statefulAdapters.get(ADAPTER_PREFIX+address.getAdapter()).toString(), e);
                }
                break;
            } else {
                log.warn("Unknown adapter: "+address.getAdapter());
            }
        }

        if (adapterId == null) {
            return null;
        }

        return new RoutingRule("", "", peerId, adapterId);
    }

    @Override
    public void removePeerAdapter(String adapterId) {
        stateless.remove(adapterId);
        Class<? extends PeerAdapter> remove = statefulAdapters.remove(adapterId);
        if (remove != null) {
            for (String id : statefulInstances.get(adapterId)) {
                executionEngine.removePeerAdapter(id);
            }
        } else {
            executionEngine.removePeerAdapter(adapterId);
        }
    }

    private String generateAdapterId(FeedbackAdapter adapter) {
        return ADAPTER_PREFIX+generateUniqueIdString();
    }

    private String generateAdapterId(Class<? extends PeerAdapter> adapter, String name) {
        return ADAPTER_PREFIX+name;
    }

    private String generateUniqueIdString() {
        return UUID.randomUUID().toString();
    }
}
