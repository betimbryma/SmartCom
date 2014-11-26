/**
 * Copyright (c) 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184 (http://dsg.tuwien.ac.at)
 *
 * This work was partially supported by the EU FP7 FET SmartSociety (http://www.smart-society-project.eu/).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.adapter.*;
import at.ac.tuwien.dsg.smartcom.adapter.util.TaskScheduler;
import at.ac.tuwien.dsg.smartcom.broker.*;
import at.ac.tuwien.dsg.smartcom.broker.policy.DynamicReplicationPolicy;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.InputAdapterExecution;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.OutputAdapterExecution;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.picocontainer.annotations.Inject;
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
public class AdapterExecutionEngine implements TaskScheduler{
    private static final Logger log = LoggerFactory.getLogger(AdapterExecutionEngine.class);

    private ExecutorService executor; //executor for adapters
    private ExecutorService pushExecutor; //executor for push tasks
    private Timer timer; //timer for timed tasks

    @Inject
    private AddressResolver addressResolver; //used to resolve addresses

    @Inject
    private MessageBroker broker; //used to send and receive messages

    @Inject
    private StatisticBean statistic;

    private final Map<Identifier, InputAdapterExecution> inputAdapterMap = new HashMap<>(); //map of identifiers of adapters and the corresponding adapter executions
    private final Map<Identifier, InputPushAdapter> pushAdapterFacadeMap = new HashMap<>();
    private final Map<Identifier, List<TimerTask>> taskMap = new HashMap<>();
    private final Map<Identifier, Future<?>> futureMap = new HashMap<>();

    private final Map<Identifier, OutputAdapterExecution> statefulOutputAdapterMap = new HashMap<>();
    private final Map<Identifier, ReplicatingMessageListener> statelessOutputAdapterMap = new HashMap<>();
    private final Map<Identifier, CancelableListener> outputAdapterCancelMap = new HashMap<>();

    /**
     * Initialises the Adapter Execution Engine
     * i.e., initialises the executors for tasks and adapters.
     */
    void init() {
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("AM-thread-%d").build());
        pushExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("PUSH-thread-%d").build());
        timer = new Timer();
    }

    /**
     * Destroys the Adapter Execution Engine
     * i.e., stops the executors for tasks and adapters and cancels all active tasks.
     */
    void destroy() {
        log.info("Executor will be shut down");

        //cancel the timer that handles timed tasks
        timer.cancel();

        //cancel all handled timer tasks
        for (List<TimerTask> timerTasks : taskMap.values()) {
            for (TimerTask timerTask : timerTasks) {
                timerTask.cancel();
            }
        }
        timer.purge();

        for (ReplicatingMessageListener listener : statelessOutputAdapterMap.values()) {
            listener.shutdown();
        }

        for (CancelableListener listener : outputAdapterCancelMap.values()) {
            listener.cancel();
        }

        for (Future<?> future : futureMap.values()) {
            future.cancel(true);
        }

        //shut down the executor that handles the adapters
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Could not await termination of executor. forcing shutdown", e);
            executor.shutdownNow();
        }

        //shut down the executor that handles the push tasks
        pushExecutor.shutdown();
        try {
            if (!pushExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                pushExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Could not await termination of push executor. forcing shutdown", e);
            pushExecutor.shutdownNow();
        }

        log.info("Executor shutdown complete!");
    }

    /**
     * Add an input push adapter instance with the given id to the adapter execution engine
     * @param adapter instance of a input push adapter
     * @param id of the adapter
     */
    void addInputAdapter(InputPushAdapter adapter, Identifier id) {
        log.info("Adding push adapter with id {}", id);
        //just add it to the managed adapters because they will organize their execution on their own
        pushAdapterFacadeMap.put(id, adapter);
    }

    /**
     * Add an input pull adapter instance with the given id to the adapter execution engine
     * @param adapter instance of the input pull adapter
     * @param id of the adapter
     * @param deleteIfSuccessful delete the adapter if it has been executed successfully
     */
    void addInputAdapter(InputPullAdapter adapter, Identifier id, boolean deleteIfSuccessful) {
        log.info("Adding pull adapter with id {}", id);
        InputAdapterExecution execution = new InputAdapterExecution(adapter, id, broker, deleteIfSuccessful, statistic);

        //start executing the pull adapter within an adapter execution
        Future<?> submit = executor.submit(execution);

        //add the created execution task (the future object) and the adapter execution
        futureMap.put(id, submit);
        inputAdapterMap.put(id, execution);
    }

    /**
     * Remove an input adapter with a given identifier from the execution. If the adapter
     * is a push adapter, it will simply destroyed (behaviour is implemented by adapter),
     * otherwise the regarding adapter execution will be cancelled.
     *
     * @param id of the input adapter (pull or push)
     * @return the stopped adapter
     */
    InputAdapter removeInputAdapter(Identifier id) {
        log.debug("Removing adapter with id {}", id);
        //check if it is a push adapter
        InputPushAdapter adapter = pushAdapterFacadeMap.get(id);
        if (adapter != null) {
            //simply destroy the adapter itself if it's a push adapter
            adapter.preDestroy();
            return adapter;
        } else {
            //remove the adapter from the execution otherwise
            Future<?> remove = futureMap.remove(id);
            remove.cancel(true);

            InputAdapterExecution execution = inputAdapterMap.remove(id);

            //TODO

            return execution.getAdapter();
        }
    }

    /**
     * Add a new output adapter instance with the given id to the execution engine. A new adapter execution
     * will be created for the adapter and it will be handled appropriately.
     *
     * @param adapter instance of the output adapter
     * @param id of the output adapter
     */
    void addOutputAdapter(final OutputAdapter adapter, final Identifier id, boolean stateful) {
        log.debug("Adding adapter with id {}", id);

        if (stateful) {
            //create execution and start executing it
            OutputAdapterExecution execution = new OutputAdapterExecution(adapter, addressResolver, id, broker, statistic);

            statefulOutputAdapterMap.put(id, execution);
            outputAdapterCancelMap.put(id, broker.registerOutputListener(id, execution));
        } else {
            OutputAdapterExecution execution = new OutputAdapterExecution(adapter, addressResolver, id, broker, statistic);
            ReplicatingMessageListener listener = new ReplicatingMessageListener(id.getId(), execution, new ReplicationFactory() {
                @Override
                public MessageListener createReplication() {
                    return new OutputAdapterExecution(adapter, addressResolver, id, broker, statistic);
                }
            }, new DynamicReplicationPolicy());

            statefulOutputAdapterMap.put(id, execution);
            statelessOutputAdapterMap.put(id, listener);
            outputAdapterCancelMap.put(id, broker.registerOutputListener(id, listener));
        }
    }

    /**
     * remove an output adapter instance with a given id from the execution of the execution engine.
     *
     * @param id of the adapter instance
     * @return the removed output adapter instance
     */
    OutputAdapter removeOutputAdapter(Identifier id) {
        log.debug("Removing adapter with id {}", id);

        OutputAdapterExecution remove = statefulOutputAdapterMap.remove(id);
        outputAdapterCancelMap.remove(id).cancel();

        return remove.getAdapter();
    }

    @Override
    public Future schedule(final PushTask task) {
        return pushExecutor.submit(task);
    }

    /**
     * Schedule a timer task with a given id at a fixed rate. The task will run as long as it is
     * not cancelled or the execution engine is shut down.
     *
     * @param task that should be scheduled
     * @param rate at which the task will be scheduled
     * @param id of the task
     */
    public void schedule(TimerTask task, long rate, Identifier id) {
        timer.scheduleAtFixedRate(task, 0, rate);

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
