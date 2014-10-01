package at.ac.tuwien.dsg.smartcom.broker;

import at.ac.tuwien.dsg.smartcom.broker.policy.ReplicationPolicy;
import at.ac.tuwien.dsg.smartcom.broker.policy.ReplicationPolicyResult;
import at.ac.tuwien.dsg.smartcom.broker.policy.TresholdReplicationPolicy;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.utils.ExpiringCounter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MessageListener that handles the scalability of a message listener by replicating
 * it when there is a high load and by removing replicas when there is a low load.
 *
 * It keeps an internal queue of messages which will be consumed by the replicas. A
 * replication handler will be called in certain intervals and decides based on
 * the replication policy whether to scale up, down or do not do anyhting at all.
 *
 * The creating of replicas is handled by a replication factory.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class ReplicatingMessageListener implements MessageListener {
    private static final AtomicInteger instanceCounter = new AtomicInteger(0);

    /**
     * Defines the limit of how many messages there should be in the
     * in-memory queue per listener.
     */
    private static final int QUEUE_SIZE_LIMIT_PER_LISTENER = 1000;

    /**
     * Defines the timeout of the replication handler after one call
     */
    private static final int COUNTER_TIMER_SECONDS = 10;

    /**
     * Defines the maximum amount of replicas
     */
    private static final int MAX_UPSCALE = 30;

    private final ExpiringCounter replicaCounter;
    private final ExpiringCounter handledCounter;
    private final BlockingDeque<Message> messageQueue;
    private final Queue<ReplicaHandler> handlerQueue;

    private final ExecutorService executor;
    private final ReplicationTimer replicationTimer;

    private final String name;
    private final ReplicationFactory factory;
    private final ReplicationPolicy policy;

    private final AtomicInteger counter = new AtomicInteger(0);

    public ReplicatingMessageListener(MessageListener listener, ReplicationFactory factory) {
        this(String.valueOf(instanceCounter.getAndIncrement()), listener, factory, new TresholdReplicationPolicy());
    }

    public ReplicatingMessageListener(String name, MessageListener listener, ReplicationFactory factory) {
        this(name, listener, factory, new TresholdReplicationPolicy());
    }

    public ReplicatingMessageListener(String name, MessageListener listener, ReplicationFactory factory, ReplicationPolicy policy) {
        this.name = name;
        this.factory = factory;
        this.policy = policy;

        replicaCounter = new ExpiringCounter(10, TimeUnit.SECONDS);
        handledCounter = new ExpiringCounter(10, TimeUnit.SECONDS);
        messageQueue = new LinkedBlockingDeque<>();
        handlerQueue = new LinkedBlockingDeque<>();

        String format = "REPLICA-"+name+"-%d";
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(format).build());

        replicationTimer = new ReplicationTimer();
        executor.submit(replicationTimer);
        scaleUp(listener);
    }

    public void shutdown() {
        replicaCounter.destroy();
        handledCounter.destroy();

        replicationTimer.stop();
        for (ReplicaHandler replicaHandler : handlerQueue) {
            replicaHandler.stop();
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    @Override
    public void onMessage(Message message) {
        counter.incrementAndGet();
        replicaCounter.increase();
        messageQueue.add(message);

        waitIfQueueIsTooFull();
    }

    void waitIfQueueIsTooFull() {
        if (messageQueue.size() > QUEUE_SIZE_LIMIT_PER_LISTENER *handlerQueue.size()) {
            try {
                synchronized (this) {
                    wait(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReplicationTimer implements Runnable {

        private boolean run = true;

        protected void stop() {
            run = false;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted() && run) {
                synchronized (this) {
                    try {
                        wait(TimeUnit.SECONDS.toMillis(COUNTER_TIMER_SECONDS));
                    } catch (InterruptedException e) {
                        continue;
                    }
                }

                ReplicationPolicyResult policyResult = policy.determineReplicationPolicy(replicaCounter.getCounter(),
                        handlerQueue.size(), messageQueue.size(), handledCounter.getCounter());

                switch(policyResult.getType()) {
                    case UPSCALE:
                        int times = policyResult.getAmount();
                        int remain = Math.min(MAX_UPSCALE - handlerQueue.size(), times);

                        if (remain > 0) {
//                            System.out.println("UPSCALE "+name+" x" + remain + " (" + handlerQueue.size() + " Instances)");

                            for (int i = 0; i < remain; i++) {
                                scaleUp(factory.createReplication());
                            }
                        }
                        break;

                    case DOWNSCALE:
                        times = policyResult.getAmount();
                        remain = Math.min(handlerQueue.size() - 1, times);

                        if (remain > 0) {
//                            System.out.println("DOWNSCALE "+name+" x" + remain + " (" + handlerQueue.size() + " Instances)");

                            for (int i = 0; i < remain; i++) {
                                scaleDown();
                            }
                        }
                        break;

                    case NOSCALE:
                        //do nothing
                        break;
                }
            }
        }
    }

    private void scaleDown() {
        ReplicaHandler handler = handlerQueue.poll();
        handler.stop();
    }

    private void scaleUp(MessageListener replication) {
        ReplicaHandler handler = new ReplicaHandler(replication);
        handlerQueue.add(handler);
        executor.submit(handler);
    }

    private class ReplicaHandler implements Runnable {

        private final MessageListener listener;
        private boolean run = true;

        private ReplicaHandler(MessageListener listener) {
            this.listener = listener;
        }

        protected void stop() {
            run = false;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted() && run) {
                try {
                    handledCounter.increase();
                    this.listener.onMessage(messageQueue.take());
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
