package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.adapter.*;
import at.ac.tuwien.dsg.smartcom.adapter.util.TaskScheduler;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.FeedbackAdapterExecution;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.PeerAdapterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
class AdapterExecutionEngine implements TaskScheduler{
    private static final Logger log = LoggerFactory.getLogger(AdapterExecutionEngine.class);

    private ExecutorService executor;
    private ExecutorService pushExecutor;
    private Timer timer;

    private final AddressResolver addressResolver;
    private final MessageBroker broker;

    private final Map<String, FeedbackAdapterExecution> feedbackAdapterMap = new HashMap<>();
    private final Map<String, FeedbackPushAdapter> pushAdapterFacadeMap = new HashMap<>();
    private final Map<String, List<TimerTask>> taskMap = new HashMap<>();
    private final Map<String, PeerAdapterExecution> peerAdapterMap = new HashMap<>();
    private final Map<String, Future<?>> futureMap = new HashMap<>();

    AdapterExecutionEngine(AddressResolver addressResolver, MessageBroker broker) {
        this.addressResolver = addressResolver;
        this.broker = broker;
    }

    void init() {
        executor = Executors.newCachedThreadPool();
        pushExecutor = Executors.newCachedThreadPool();
        timer = new Timer();
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
            executor.shutdownNow();
        }

        pushExecutor.shutdown();

        try {
            pushExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Could not await termination of push executor. forcing shutdown", e);
            pushExecutor.shutdownNow();
        }

        timer.cancel();

        log.info("Executor shutdown complete!");
    }

    void addFeedbackAdapter(FeedbackPushAdapter adapter, String id) {
        log.info("Adding push adapter with id ()", id);
        pushAdapterFacadeMap.put(id, adapter);
    }

    void addFeedbackAdapter(FeedbackPullAdapter adapter, String id) {
        log.info("Adding pull adapter with id ()", id);
        FeedbackAdapterExecution execution = new FeedbackAdapterExecution(adapter, id, broker);

        execution.init();

        Future<?> submit = executor.submit(execution);

        futureMap.put(id, submit);
        feedbackAdapterMap.put(id, execution);
    }

    FeedbackAdapter removeFeedbackAdapter(String id) {
        log.info("Removing adapter with id "+id);
        FeedbackPushAdapter adapter = pushAdapterFacadeMap.get(id);
        if (adapter != null) {
            adapter.preDestroy();
            return adapter;
        } else {
            Future<?> remove = futureMap.remove(id);
            remove.cancel(true);

            FeedbackAdapterExecution execution = feedbackAdapterMap.get(id);

            return execution.getAdapter();
        }
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

    @Override
    public PushTask schedule(final PushTask task) {
        final Future<?> submit = pushExecutor.submit(task);

        return new PushTask() {
            @Override
            public void run() {
                task.run();
            }

            @Override
            public void cancel() {
                submit.cancel(true);
            }
        };
    }

    public void schedule(TimerTask task, long period, String id) {
        timer.scheduleAtFixedRate(task, 0, period);

        List<TimerTask> timerTasks = taskMap.get(id);
        if (timerTasks == null) {
            synchronized (taskMap) {
                timerTasks = taskMap.get(id);
                if (timerTasks == null) {
                    timerTasks = Collections.synchronizedList(new ArrayList<TimerTask>());
                    taskMap.put(id, timerTasks);
                }
            }
        }
        timerTasks.add(task);
    }
}
