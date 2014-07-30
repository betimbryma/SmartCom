package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.adapter.*;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

    @Inject
    private AdapterExecutionEngine executionEngine;

    @Inject
    private PMCallback peerManager;

    @Inject
    private MessageBroker broker;

    @Inject
    private AddressResolver addressResolver;

    private final Map<Identifier, Class<? extends OutputAdapter>> statefulAdapters = new ConcurrentHashMap<>();
    private final Map<Identifier, List<Identifier>> statefulInstances = new ConcurrentHashMap<>();

    private final List<Identifier> stateless = new ArrayList<>();

    @Override
    @PostConstruct
    public void init() {
        executionEngine.init();
    }

    @Override
    @PreDestroy
    public void destroy() {
        executionEngine.destroy();
    }

    @Override
    public Identifier addPushAdapter(InputPushAdapter adapter) {
        Identifier id = Identifier.adapter(generateAdapterId(adapter));

        if (adapter instanceof InputPushAdapterImpl) {
            ((InputPushAdapterImpl) adapter).setInputPublisher(broker);
            ((InputPushAdapterImpl) adapter).setScheduler(executionEngine);
        }

        //init the adapter
        adapter.init();

        executionEngine.addInputAdapter(adapter, id);

        return id;
    }

    @Override
    public Identifier addPullAdapter(InputPullAdapter adapter, long period) {
        final Identifier id = Identifier.adapter(generateAdapterId(adapter));

        executionEngine.addInputAdapter(adapter, id);

        if (period > 0) {
            executionEngine.schedule(new TimerTask() {
                @Override
                public void run() {
                    broker.publishRequest(id, new Message());
                }
            }, period, id);
        }

        return id;
    }

    @Override
    public InputAdapter removeInputAdapter(Identifier adapterId) {
        return executionEngine.removeInputAdapter(adapterId);
    }

    @Override
    public Identifier registerOutputAdapter(Class<? extends OutputAdapter> adapter) {
        Adapter annotation = adapter.getAnnotation(Adapter.class);
        if (annotation == null) {
            log.error("Can't find annotation @Adapter in class ()", adapter.getSimpleName());
            return null;
        }
        boolean stateful = annotation.stateful();
        String name = annotation.name();

        Identifier id = Identifier.adapter(generateAdapterId(adapter, name));

        if (!stateful) {
            try {
                OutputAdapter instance = instantiateClass(adapter);
                executionEngine.addOutputAdapter(instance, id, false);
                stateless.add(id);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("Could not instantiate class "+adapter.toString(), e);
            }
        } else {
            statefulAdapters.put(id, adapter);
        }

        return id;
    }

    private OutputAdapter instantiateClass(Class<? extends OutputAdapter> adapter) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        for (Constructor<?> declaredConstructor : adapter.getDeclaredConstructors()) {
            if (declaredConstructor.getParameterTypes().length == 0) {
                return (OutputAdapter) declaredConstructor.newInstance();
            }
        }

        return null;
    }

    private OutputAdapter instantiateClass(Class<? extends OutputAdapter> adapter, PeerAddress address) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        try {
            Constructor<? extends OutputAdapter> constructor = adapter.getDeclaredConstructor(PeerAddress.class);
            if (constructor != null) {
                return constructor.newInstance(address);
            }
        } catch (NoSuchMethodException e) {
            log.debug("Adapter class {} has no constructor that accepts a peer address, using default constructor", adapter);
        }

        return instantiateClass(adapter);
    }

    @Override
    public Identifier createEndpointForPeer(Identifier peerId) {
        Identifier adapterId = null;
        Collection<PeerAddress> peerAddress;
        try {
            peerAddress = peerManager.getPeerAddress(peerId);
        } catch (NoSuchPeerException e) {
            log.warn("No such peer: ()", peerId);
            return null;
        }
        for (PeerAddress address : peerAddress) {
            if(stateless.contains(address.getAdapterId())) {
                addressResolver.addPeerAddress(address);
                adapterId = address.getAdapterId();
                break;
            } else if (statefulAdapters.containsKey(address.getAdapterId())) {
                try {
                    Identifier adapter = address.getAdapterId();
                    Identifier newId = Identifier.adapter(adapter, peerId);

                    //check if there is already such an instance
                    synchronized (statefulInstances) {
                        List<Identifier> instances = statefulInstances.get(address.getAdapterId());
                        if (instances != null) {
                            if (instances.contains(newId)) {
                                adapterId = newId;
                                break;
                            }
                        } else {
                            instances = new ArrayList<>();
                            statefulInstances.put(adapter, instances);
                        }

                        Class<? extends OutputAdapter> outputAdapterClass = statefulAdapters.get(adapter);
                        OutputAdapter outputAdapter;
                        try {
                            outputAdapter = instantiateClass(outputAdapterClass, address);
                        } catch (InvocationTargetException e) {
                            log.error("Could not instantiate class "+adapter, e);
                            continue;
                        }
                        executionEngine.addOutputAdapter(outputAdapter, newId, true);
                        adapterId = newId;
                        instances.add(adapterId);
                    }
                    addressResolver.addPeerAddress(address);
                } catch (IllegalAccessException | InstantiationException e) {
                    log.error("Could not instantiate class " + statefulAdapters.get(address.getAdapterId()).toString(), e);
                }
                break;
            } else {
                log.warn("Unknown adapter: "+address.getAdapterId());
            }
        }

        if (adapterId == null) {
            return null;
        }

        return adapterId;
    }

    @Override
    public void removeOutputAdapter(Identifier adapterId) {
        stateless.remove(adapterId);
        Class<? extends OutputAdapter> remove = statefulAdapters.remove(adapterId);
        if (remove != null) {
            for (Identifier id : statefulInstances.get(adapterId)) {
                executionEngine.removeOutputAdapter(id);
            }
        } else {
            executionEngine.removeOutputAdapter(adapterId);
        }
    }

    private String generateAdapterId(InputAdapter adapter) {
        return generateUniqueIdString();
    }

    private String generateAdapterId(Class<? extends OutputAdapter> adapter, String name) {
        return name;
    }

    private String generateUniqueIdString() {
        return UUID.randomUUID().toString();
    }
}
