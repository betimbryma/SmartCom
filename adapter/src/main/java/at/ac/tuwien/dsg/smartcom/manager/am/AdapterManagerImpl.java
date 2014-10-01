package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.adapter.InputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.exception.ErrorCode;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the Adapter Manager.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class AdapterManagerImpl implements AdapterManager {
    private static final Logger log = LoggerFactory.getLogger(AdapterManager.class);

    @Inject
    private AdapterExecutionEngine executionEngine; //used to execute adapters

    @Inject
    private MessageBroker broker; //sending and receiving messages

    @Inject
    private AddressResolver addressResolver; //to resolve addresses of peers

    //stateful adapters will be created on demand for every peer. StatefulInstances handles instances per adapter type.
    private final Map<Identifier, Class<? extends OutputAdapter>> statefulAdapters = new ConcurrentHashMap<>();
    private final Map<Identifier, List<Identifier>> statefulInstances = new ConcurrentHashMap<>();

    private final List<Identifier> stateless = new ArrayList<>(); //List of stateless adapters that are executed

    private final ExecutorService replicaExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("REPLICA-thread-%d").build());

    @PostConstruct
    public void init() {
        executionEngine.init();
    }

    @PreDestroy
    public void destroy() {
        executionEngine.destroy();

        replicaExecutor.shutdown();
        try {
            if (!replicaExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                replicaExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Could not await termination of executor. forcing shutdown", e);
            replicaExecutor.shutdownNow();
        }
    }

    @Override
    public Identifier addPushAdapter(InputPushAdapter adapter) {
        Identifier id = Identifier.adapter(generateAdapterId(adapter));

        //Set the input publisher and the scheduler for push adapters
        adapter.setInputPublisher(broker);
        adapter.setScheduler(executionEngine);

        //init the adapter
        adapter.init();

        //add the input adapter to the execution engine
        executionEngine.addInputAdapter(adapter, id);

        return id;
    }

    @Override
    public Identifier addPullAdapter(InputPullAdapter adapter, long period, boolean deleteIfSuccessful) {
        final Identifier id = Identifier.adapter(generateAdapterId(adapter));

        //add the input pull adapter to the execution engine
        executionEngine.addInputAdapter(adapter, id, deleteIfSuccessful);

        //if the request period has been specified, generate a task that handles the pull request regularly
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
    public Identifier registerOutputAdapter(Class<? extends OutputAdapter> adapter) throws CommunicationException {

        //check if annotation is present
        Adapter annotation = adapter.getAnnotation(Adapter.class);
        if (annotation == null) {
            //throw exception as we can't handle such a case properly
            log.error("Can't find annotation @Adapter in class {}", adapter.getSimpleName());
            throw new CommunicationException(new ErrorCode(331, "@Adapter annotation not found!"));
        }
        //extract the data from the annotation
        boolean stateful = annotation.stateful();
        String name = annotation.name();

        Identifier id = Identifier.adapter(generateAdapterId(adapter, name));

        if (!stateful) {
            //if the adapter is not stateful, instantiate it immediately and start executing it
            try {
                OutputAdapter instance = instantiateClass(adapter);
                executionEngine.addOutputAdapter(instance, id, false);
                stateless.add(id);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("Could not instantiate class "+adapter.toString(), e);
                throw new CommunicationException(new ErrorCode(332, "Could not instantiate class: "+e.getLocalizedMessage()));
            } catch (NoSuchMethodException e) {
                throw new CommunicationException(new ErrorCode(333, "Could not instantiate class "+adapter+" because there is no default constructor"));
            }
        } else {
            //if the adapter is stateful, just register it in the manager.
            //it will be instantiated on demand!
            statefulAdapters.put(id, adapter);
        }

        return id;
    }

//    public void createReplication(final Class<? extends OutputAdapter> adapter, final Identifier id) {
//        replicaExecutor.submit(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    OutputAdapter instance = instantiateClass(adapter);
//                    executionEngine.addOutputAdapterReplica(instance, id);
//                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//                    log.error("Could not instantiate class "+adapter.toString(), e);
////            throw new CommunicationException(new ErrorCode(332, "Could not instantiate class: "+e.getLocalizedMessage()));
//                }
//            }
//        });
//    }
//
//
//    public void removeReplication(final Identifier id) {
//        replicaExecutor.submit(new Runnable() {
//            @Override
//            public void run() {
//                executionEngine.removeOutputAdapterReplica(id);
//            }
//        });
//    }

    /**
     * Instantiate a given output adapter. The method will look for the default constructor and will throw a NoSuchMethodException
     * if there is no default constructor available.
     *
     * If the default constructor is present but anything went wrong during the instantiation of the class, the method
     * will throw an IllegalAccessException, InvocationTargetException or an InstantiationException.
     *
     * Note that inner classes have an implicit first parameter in the default constructor and can therefore not be
     * constructed using this method.
     *
     * @param adapter class that has to be instantiated
     * @return newly created instance
     * @throws NoSuchMethodException could not find default constructor
     * @throws IllegalAccessException could not instantiate the adapter using the default constructor
     * @throws InvocationTargetException could not instantiate the adapter using the default constructor
     * @throws InstantiationException could not instantiate the adapter using the default constructor
     */
    private OutputAdapter instantiateClass(Class<? extends OutputAdapter> adapter) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<? extends OutputAdapter> constructor = adapter.getConstructor();
        return constructor.newInstance();
    }

    /**
     * Instantiates the given output adapter. If the adapter has a constructor which requires a peer address as it's only
     * parameter, this constructor will be used to instantiate the class. Otherwise the default constructor will be used.
     *
     * Note that this method should only be used for stateful adapters because instantiating a class with a peer address
     * makes it only suitable for one specific peer!
     *
     * @param adapter class that has to be instantiated
     * @param address of a peer that is associated to the adapter
     * @return newly created instance
     * @throws NoSuchMethodException could not find default constructor
     * @throws IllegalAccessException could not instantiate the adapter using the default constructor
     * @throws InvocationTargetException could not instantiate the adapter using the default constructor
     * @throws InstantiationException could not instantiate the adapter using the default constructor
     * @see AdapterManagerImpl#instantiateClass(Class)
     */
    private OutputAdapter instantiateClass(Class<? extends OutputAdapter> adapter, PeerChannelAddress address) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        try {
            //look for a constructor that has a single parameter of type PeerAddress
            Constructor<? extends OutputAdapter> constructor = adapter.getDeclaredConstructor(PeerChannelAddress.class);
            if (constructor != null) {
                return constructor.newInstance(address);
            }
        } catch (NoSuchMethodException e) {
            log.debug("Adapter class {} has no constructor that accepts a peer address, using default constructor", adapter);
        }

        //instantiate the class using the default constructor otherwise
        return instantiateClass(adapter);
    }

    @Override
    public List<Identifier> createEndpointForPeer(PeerInfo peerInfo) {
        List<Identifier> adapters = new ArrayList<>();

        //get all available addresses for a peer
        Collection<PeerChannelAddress> peerChannelAddreses;
        peerChannelAddreses = peerInfo.getAddresses();

        //check for every address if there is an adapter registered
        addressLoop:
        for (PeerChannelAddress address : peerChannelAddreses) {
        	Identifier adapterId = Identifier.adapter(address.getChannelType().getId());
        	
            //check first if there is a stateless adapter
            if(stateless.contains(adapterId)) {
                //we are already done, just add the address to our address resolver (cache) and return the adapter id
                addressResolver.addPeerAddress(address);
                adapters.add(Identifier.adapter(address.getChannelType().getId()));
                switch(peerInfo.getDeliveryPolicy()) {
                    case TO_ALL_CHANNELS:
                        continue addressLoop;
                    case AT_LEAST_ONE:
                        continue addressLoop;
                    case PREFERRED:
                        break addressLoop; //assuming the first working adapter is the preferred
                }
            } else if (statefulAdapters.containsKey(adapterId)) {
                try {
                    //get the id of the adapter type and create a new id for the stateful adapter instance for this peer
                    Identifier adapter = Identifier.adapter(address.getChannelType().getId());
                    Identifier newId = Identifier.adapter(adapter, peerInfo.getId());


                    synchronized (statefulInstances) {
                        //check if there is already such an instance
                        List<Identifier> instances = statefulInstances.get(address.getChannelType());
                        if (instances != null) {
                            if (instances.contains(newId)) {
                                //there is already such an instance, just return its id
                                adapters.add(newId);
                                addressResolver.addPeerAddress(address);

                                switch(peerInfo.getDeliveryPolicy()) {
                                    case TO_ALL_CHANNELS:
                                        continue addressLoop;
                                    case AT_LEAST_ONE:
                                        continue addressLoop;
                                    case PREFERRED:
                                        break addressLoop;
                                }
                            }
                        } else {
                            instances = new ArrayList<>();
                            statefulInstances.put(adapter, instances);
                        }

                        //instantiate new adapter type
                        Class<? extends OutputAdapter> outputAdapterClass = statefulAdapters.get(adapterId);
                        OutputAdapter outputAdapter;
                        try {
                            outputAdapter = instantiateClass(outputAdapterClass, address);
                        } catch (InvocationTargetException | NoSuchMethodException e) {
                            log.error("Could not instantiate class "+adapter, e);
                            continue;
                        }

                        //start executing the new adapter
                        executionEngine.addOutputAdapter(outputAdapter, newId, true);
                        adapters.add(newId);
                        instances.add(newId);
                    }
                    addressResolver.addPeerAddress(address);
                    switch(peerInfo.getDeliveryPolicy()) {
                        case TO_ALL_CHANNELS:
                            continue addressLoop;
                        case AT_LEAST_ONE:
                            continue addressLoop;
                        case PREFERRED:
                            break addressLoop;
                    }
                } catch (IllegalAccessException | InstantiationException e) {
                    log.error("Could not instantiate class " + statefulAdapters.get(address.getChannelType()).toString(), e);
                }
            } else {
                log.warn("Unknown adapter: "+address.getChannelType());
            }
        }

        return adapters;
    }

    @Override
    public void removeOutputAdapter(Identifier adapterId) {
        stateless.remove(adapterId);
        Class<? extends OutputAdapter> remove = statefulAdapters.remove(adapterId);
        if (remove != null) {
            //if it's a stateful adapter, remove all its instances (for every peer there is one)
            for (Identifier id : statefulInstances.get(adapterId)) {
                executionEngine.removeOutputAdapter(id);
            }
        } else {
            executionEngine.removeOutputAdapter(adapterId);
        }
    }

    /**
     * Generate a new unique id for the given adapter
     * @param adapter that needs an id
     * @return id for the adapter
     */
    private String generateAdapterId(InputAdapter adapter) {
        return generateUniqueIdString();
    }

    /**
     * Generate an adapter id based on the adapter type and the name of the adapter
     * @param adapter type of the adapter
     * @param name of the adapter
     * @return id of the adapter
     */
    private String generateAdapterId(Class<? extends OutputAdapter> adapter, String name) {
        return name;
    }

    /**
     * Generate an unique id
     * @return unique id
     */
    private String generateUniqueIdString() {
        return UUID.randomUUID().toString();
    }
}
