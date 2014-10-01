package at.ac.tuwien.dsg.smartcom.broker;

import at.ac.tuwien.dsg.smartcom.broker.utils.ApacheActiveMQUtils;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.Before;

import javax.jms.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ReplicatingMessageListenerTest implements MessageListener {

    private CountDownLatch counter;

    @Before
    public void setUp() throws Exception {
        ApacheActiveMQUtils.startActiveMQWithoutPersistence(61616);
    }

    @After
    public void tearDown() throws Exception {
        ApacheActiveMQUtils.stopActiveMQ();
    }

//    @Test
    public void testReplication() throws JMSException, InterruptedException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?create=false&jms.prefetchPolicy.all=1000");;
        Connection connection = connectionFactory.createConnection();
        connection.start();

        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Destination queue = session.createQueue("SmartCom.task.adapter");
        MessageConsumer consumer = session.createConsumer(queue);

        final int messages = 1000000;
        counter = new CountDownLatch(messages);

        final ReplicatingMessageListener listener = new ReplicatingMessageListener(this, new ReplicationFactory() {
            @Override
            public MessageListener createReplication() {
                return ReplicatingMessageListenerTest.this;
            }
        });

        final MessageProducer producer = session.createProducer(null);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);

        Runnable run = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < messages; i++) {
                    Message.MessageBuilder builder = new Message.MessageBuilder()
                            .setType("COMPUTE")
                            .setSubtype("REQUEST")
                            .setSenderId(Identifier.component("DEMO"))
                            .setConversationId(System.nanoTime() + "")
                            .setContent("Do some stuff and respond!");
                    ObjectMessage msg = null;
                    try {
                        msg = session.createObjectMessage(builder.create());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }

                    try {
                        producer.send(queue, msg);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread thread = new Thread(run);
        thread.start();

        consumer.setMessageListener(new javax.jms.MessageListener() {
            @Override
            public void onMessage(javax.jms.Message message) {
                try {
                    listener.onMessage((Message) ((ObjectMessage) message).getObject());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });

        long start = System.currentTimeMillis();
        while (!counter.await(1, TimeUnit.SECONDS)) {
            int count = (int) counter.getCount();
            long end = System.currentTimeMillis();
            long diff = end - start;

            float sample = (((float) (messages - count)) / (((float) diff) / 1000f)) * 2;
            System.out.println("Messages left: " + count + "/" + messages + " (" + sample + ") ");
        }

        System.out.println(counter.getCount());
    }

    @Override
    public void onMessage(Message message) {
        counter.countDown();
    }
}