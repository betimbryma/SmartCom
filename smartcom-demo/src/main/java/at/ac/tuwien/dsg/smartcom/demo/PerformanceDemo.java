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
package at.ac.tuwien.dsg.smartcom.demo;

import at.ac.tuwien.dsg.smartcom.Communication;
import at.ac.tuwien.dsg.smartcom.SmartCom;
import at.ac.tuwien.dsg.smartcom.SmartComBuilder;
import at.ac.tuwien.dsg.smartcom.adapter.InputPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.PushTask;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.model.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceDemo {

    private final static int TOTAL_MESSAGES = 100000;

    public static void main(String[] args) throws IOException, CommunicationException, BrokenBarrierException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Select the number of peers:");
        int peersAmount = Integer.valueOf(reader.readLine().trim());

        System.out.println("Select the number of concurrent message producers:");
        int messageProducersAmount = Integer.valueOf(reader.readLine().trim());

        List<Statistic> stats = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            stats.add(runDemo(peersAmount, messageProducersAmount, true));
        }

        float max = 0;
        float sum = 0;
        int length = 0;
        for (Statistic stat : stats) {
            length = Math.max(stat.samples.size(), length);
            max = Math.max(stat.average, max);
            sum += stat.average;
        }

        System.out.println("***************************");
        System.out.println("Maximum speed: "+max);
        System.out.println("Average speed: "+sum/10f);

        File file = new File("performance\\performance_"+peersAmount+"_"+messageProducersAmount+".csv");
        int j = 1;
        while (file.exists()) {
            file = new File("performance\\performance_"+peersAmount+"_"+messageProducersAmount+"_"+j+".csv");
            j++;
        }

        FileWriter fw = new FileWriter(file);
        fw.append("Timestamp:;");
        for (int i = 0; i < length; i++) {
            fw.append(String.valueOf(i)).append(";");
        }
        fw.append(System.lineSeparator());

        int i = 0;
        for (Statistic stat : stats) {
            fw.append("Sample ").append((i++)+";");

            for (Float sample : stat.samples) {
                fw.append(sample+"").append(";");
            }

            fw.append(System.lineSeparator());
        }

        fw.append(System.lineSeparator());
        fw.append("Sample;Average");
        fw.append(System.lineSeparator());
        i = 0;
        for (Statistic stat : stats) {
            fw.append("Sample ").append((i++)+";");
            fw.append(stat.average+"");
            fw.append(System.lineSeparator());
        }

        fw.append(System.lineSeparator());
        fw.append("Total max:;").append(max+"").append(";");
        fw.append(System.lineSeparator());
        fw.append("Total average:;").append((sum/10f)+"").append(";");
        fw.close();
    }

    private static class Statistic {
        float average;
        List<Float> samples;
    }

    private static Statistic runDemo(int peersAmount, int messageProducersAmount, boolean debug) throws CommunicationException, InterruptedException, BrokenBarrierException {
        sentMessages = new AtomicInteger();
        receivedMessages = new AtomicInteger();
        inputMessages = new AtomicInteger();
        queue = new LinkedBlockingDeque<>();
        counterMap = Collections.synchronizedMap(new HashMap<Identifier, AtomicInteger>());

        DemoPeerManager peerManager = new DemoPeerManager();
        for (int i = 0; i < peersAmount; i++) {
            Identifier id = Identifier.peer("peer"+i);
            List<PeerChannelAddress> addresses = new ArrayList<>();

            List<Serializable> parameters = new ArrayList<>(1);
            PeerChannelAddress address = new PeerChannelAddress(id, Identifier.channelType("adapter"), parameters);
            addresses.add(address);

            peerManager.addPeer(id, new PeerInfo(id, DeliveryPolicy.Peer.TO_ALL_CHANNELS, null, addresses), id.getId());
        }

        float messages_per_worker_per_peer = ((float) TOTAL_MESSAGES)/((float) messageProducersAmount)/((float)peersAmount);
        int interval = 1;

        if (messages_per_worker_per_peer < 1) {
            interval = (int) (1 / messages_per_worker_per_peer);
        }

        final int messages = (int) (messageProducersAmount*peersAmount* messages_per_worker_per_peer*10);
        CountDownLatch counter = new CountDownLatch(messages);

        SmartCom smartCom = new SmartComBuilder(peerManager, peerManager, peerManager).create();
        Communication communication = smartCom.getCommunication();
        communication.registerNotificationCallback(new NotificationHandler(counter));
        communication.registerOutputAdapter(OutputAdapter.class);

        Statistic stat = new Statistic();
        stat.samples = new ArrayList<Float>();

        CyclicBarrier barrier = new CyclicBarrier(messageProducersAmount+1);

        for (int i = 0; i < 10; i++) {
            communication.addPushAdapter(new InputAdapter());
        }

        for (int i = 0; i < messageProducersAmount; i++) {
            Message.MessageBuilder builder = new Message.MessageBuilder()
                    .setType("COMPUTE")
                    .setSubtype("REQUEST")
                    .setSenderId(Identifier.component("DEMO"))
                    .setConversationId(System.nanoTime() + "")
                    .setContent("Do some stuff and respond!");
            new Thread(new WorkerThread(builder, barrier, communication, peersAmount, messages_per_worker_per_peer, interval, i%interval)).start();
        }

        System.out.println("START");
        long start = System.currentTimeMillis();
        barrier.await();

        int oldCount = 0;
        int sameCount = 0;

        int nextTreshold = messages - (messages/20);
        int workerRuns = 0;

        while (!counter.await(10, TimeUnit.SECONDS)) {
            int count = (int) counter.getCount();
            long end = System.currentTimeMillis();
            long diff = end - start;

            float sample = (((float) (messages - count)) / (((float) diff) / 1000f)) * 2;
            stat.samples.add(sample);

            if (debug) {
                System.out.println("Messages left: " + count + "/" + messages + " (" + sample + ") " + inputMessages.get() + "/" + sentMessages.get());
            }

            if (count == oldCount) {
                sameCount++;

                if (sameCount == 3) {
                    break;
                }
            } else {
                sameCount = 0;
            }

            oldCount = count;

            if (count < nextTreshold && workerRuns < 9) {
                System.out.println("Adding more messages!");

                for (int i = 0; i < messageProducersAmount; i++) {
                    Message.MessageBuilder builder = new Message.MessageBuilder()
                            .setType("COMPUTE")
                            .setSubtype("REQUEST" + workerRuns)
                            .setSenderId(Identifier.component("DEMO"))
                            .setConversationId(System.nanoTime() + "")
                            .setContent("Do some stuff and respond!");
                    new Thread(new WorkerThread(builder, barrier, communication, peersAmount, messages_per_worker_per_peer, interval, i%interval)).start();
                }
                barrier.await();

                workerRuns++;
                nextTreshold-=(messages/10);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("END");

        long diff = end-start;

        stat.average = ((float)messages)/(((float)diff)/1000f)*2;

        System.out.println("Duration: "+diff+" milliseconds");
        System.out.println("Messages: "+messages);
        System.out.println("Messages per seconds: "+stat.average);

        smartCom.tearDownSmartCom();

        return stat;
    }

    private static class WorkerThread implements Runnable {

        private final Message msg;
        private final CyclicBarrier barrier;
        private final Communication communication;
        private final int peers;
        private final float messages_per_worker_per_peer;
        private final int interval;
        private final int offset;

        private WorkerThread(Message.MessageBuilder builder, CyclicBarrier barrier, Communication communication, int peers, float messages_per_worker_per_peer, int interval, int offset) {
            this.interval = interval;
            this.offset = offset;
            this.msg = builder.create();
            this.barrier = barrier;
            this.communication = communication;
            this.peers = peers;
            this.messages_per_worker_per_peer = messages_per_worker_per_peer;
        }

        @Override
        public void run() {
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException ignored) {
            }

            int sent = 0;
            for (int j = 0; j < messages_per_worker_per_peer; j++) {
                for (int i = offset; i < peers; i+=interval) {
                    try {
                        msg.setReceiverId(Identifier.peer("peer" + i));
                        communication.send(msg.clone());
                        sent++;
                    } catch (CommunicationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class NotificationHandler implements NotificationCallback {

        private final CountDownLatch counter;

        public NotificationHandler(CountDownLatch counter) {
            this.counter = counter;
        }

        @Override
        public void notify(Message message) {
            if ("RESPONSE".equals(message.getSubtype())) {
                counter.countDown();
                receivedMessages.incrementAndGet();
            } else {
                //discard the acknowledge messages
            }
        }
    }

    private static AtomicInteger sentMessages = new AtomicInteger();
    private static AtomicInteger receivedMessages = new AtomicInteger();
    private static AtomicInteger inputMessages = new AtomicInteger();

    private static BlockingDeque<Message> queue = new LinkedBlockingDeque<>();

    private static Map<Identifier, AtomicInteger> counterMap = Collections.synchronizedMap(new HashMap<Identifier, AtomicInteger>());

    @Adapter(name="adapter", stateful = false)
    public static class OutputAdapter implements at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter {

        @Override
        public void push(Message message, PeerChannelAddress address) {
            try {
                AtomicInteger atomicInteger = counterMap.get(message.getSenderId());
                if (atomicInteger == null) {
                    synchronized (counterMap) {
                        atomicInteger = counterMap.get(message.getSenderId());
                        if (atomicInteger == null) {
                            atomicInteger = new AtomicInteger(0);
                            counterMap.put(message.getSenderId(), atomicInteger);
                        }
                    }
                }
                atomicInteger.incrementAndGet();

                sentMessages.incrementAndGet();
                queue.push(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class InputAdapter extends InputPushAdapter {

        private boolean run = true;

        @Override
        protected void cleanUp() {
            run = false;
        }

        @Override
        public void init() {
            schedule(new PushTask() {
                @Override
                public void run() {
                    while (run) {
                        try {
                            Message message = queue.take();
                            inputMessages.incrementAndGet();
                            publishMessage(new Message.MessageBuilder()
                                    .setType("COMPUTE")
                                    .setSubtype("RESPONSE")
                                    .setSenderId(Identifier.adapter("adapter"))
                                    .setConversationId(message.getConversationId())
                                    .setContent("Do some stuff and respond!")
                                    .create());
                        } catch (InterruptedException e) {
                            run = false;
                        }
                    }
                }
            });
        }
    }
}