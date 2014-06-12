package at.ac.tuwien.dsg.smartcom.scm.manager.am;

import at.ac.tuwien.dsg.smartcom.adapter.FeedbackAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.PeerAdapter;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.scm.manager.am.adapter.FeedbackAdapterExecution;
import at.ac.tuwien.dsg.smartcom.scm.manager.am.adapter.FeedbackAdapterFacade;
import at.ac.tuwien.dsg.smartcom.scm.manager.am.adapter.PeerAdapterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
class AdapterExecutionEngine {
    private static final Logger log = LoggerFactory.getLogger(AdapterExecutionEngine.class);

    private ExecutorService executor;
    private final AddressResolver addressResolver;
    private final MessageBroker broker;

    private final Map<String, FeedbackAdapterExecution> feedbackAdapterMap = new HashMap<>();
    private final Map<String, PeerAdapterExecution> peerAdapterMap = new HashMap<>();
    private final Map<String, Future<?>> futureMap = new HashMap<>();

    AdapterExecutionEngine(AddressResolver addressResolver, MessageBroker broker) {
        this.addressResolver = addressResolver;
        this.broker = broker;
    }

    void init() {
        executor = Executors.newCachedThreadPool();
    }

    void destroy() {
        log.info("Executor will be shut down");

        for (Future<?> future : futureMap.values()) {
            future.cancel(true);
        }

        executor.shutdown();

        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Could not await termination of executor. forcing shutdown", e);


        }

        log.info("Executor shutdown complete!");
    }

    void addFeedbackAdapter(FeedbackAdapterFacade adapter, String id) {
        log.info("Adding adapter with id "+id);
        FeedbackAdapterExecution execution = new FeedbackAdapterExecution(adapter, id, broker);

        execution.init();

        Future<?> submit = executor.submit(execution);

        futureMap.put(id, submit);
        feedbackAdapterMap.put(id, execution);
    }

    FeedbackAdapter removeFeedbackAdapter(String id) {
        log.info("Removing adapter with id "+id);
        Future<?> remove = futureMap.remove(id);
        remove.cancel(true);

        FeedbackAdapterExecution execution = feedbackAdapterMap.get(id);
        execution.preDestroy();

        return execution.getAdapter();
    }

    void addPeerAdapter(PeerAdapter adapter, String id, boolean stateful) {
        log.info("Adding adapter with id "+id);
        PeerAdapterExecution execution = new PeerAdapterExecution(adapter, addressResolver, id, stateful, broker);
        Future<?> submit = executor.submit(execution);

        futureMap.put(id, submit);
        peerAdapterMap.put(id, execution);
    }

    PeerAdapter removePeerAdapter(String id) {
        log.info("Removing adapter with id "+id);
        Future<?> remove = futureMap.remove(id);
        remove.cancel(true);

        return peerAdapterMap.get(id).getAdapter();
    }
}
