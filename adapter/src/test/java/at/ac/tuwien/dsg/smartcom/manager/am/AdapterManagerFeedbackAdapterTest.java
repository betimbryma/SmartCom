package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPushAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPushAdapterImpl;
import at.ac.tuwien.dsg.smartcom.adapter.PushTask;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AdapterManagerFeedbackAdapterTest {

    private List<String> feedbackAdapterIds = new ArrayList<>();
    private FeedbackPullAdapter pullAdapter;
    private AdapterManager manager;
    private FeedbackPushAdapter pushAdapter;
    private MessageBroker broker;


    @Before
    public void setUp() throws Exception {
        broker = new SimpleMessageBroker();
        manager = new AdapterManagerImpl(new SimpleAddressResolverDAO(), new PMCallback(), broker);
        pullAdapter = new TestFeedbackPullAdapter();
        pushAdapter = new TestFeedbackPushAdapter();

        manager.init();
    }

    @After
    public void tearDown() throws Exception {
        manager.destroy();

        for (String id : feedbackAdapterIds) {
            manager.removeFeedbackAdapter(id);
        }
    }

    @Test
    public void testAddPushAdapter() throws Exception {
        feedbackAdapterIds.add(manager.addPushAdapter(pushAdapter));

        Message feedback = broker.receiveFeedback();

        assertEquals("wrong feedback", "push", feedback.getContent());
    }

    @Test
    public void testAddPullAdapter() throws Exception {
        String id = manager.addPullAdapter(pullAdapter, 0);
        feedbackAdapterIds.add(id);

        broker.publishRequest(id, new Message());

        Message feedback = broker.receiveFeedback();

        assertEquals("wrong feedback", "pull", feedback.getContent());
    }

    @Test(timeout = 2000)
    public void testAddPullAdapterWithTimeout() throws Exception {
        String id = manager.addPullAdapter(pullAdapter, 1000);
        feedbackAdapterIds.add(id);

        Message feedback = broker.receiveFeedback();

        assertEquals("wrong feedback", "pull", feedback.getContent());
    }

    private class TestFeedbackPullAdapter implements FeedbackPullAdapter {

        @Override
        public Message pull() {
            Message msg = new Message();
            msg.setContent("pull");
            return msg;
        }
    }

    private class TestFeedbackPushAdapter extends FeedbackPushAdapterImpl {

        String text = "uninitialized";

        @Override
        public void init() {
            text = "push";

            schedule(new PushTask() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            wait(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Message msg = new Message();
                    msg.setContent(text);
                    publishMessage(msg);
                }
            });
        }

        @Override
        public void preDestroy() {
            text = "destroyed";
        }

        @Override
        protected void cleanUp() {

        }
    }

    private class PMCallback implements at.ac.tuwien.dsg.smartcom.callback.PMCallback {

        @Override
        public Collection<PeerAddress> getPeerAddress(String id) {
            return null;
        }

        @Override
        public boolean authenticate(String username, String password) {
            return false;
        }
    }
}