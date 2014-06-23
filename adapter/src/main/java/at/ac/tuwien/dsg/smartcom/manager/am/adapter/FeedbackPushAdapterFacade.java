package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.FeedbackAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPushAdapter;
import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class FeedbackPushAdapterFacade implements FeedbackAdapterFacade {

    private final FeedbackPushAdapter adapter;

    public FeedbackPushAdapterFacade(FeedbackPushAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public Message checkForResponse() {
        return null;
    }

    @Override
    public void preDestroy() {
        adapter.preDestroy();
    }

    @Override
    public FeedbackAdapter getAdapter() {
        return adapter;
    }
}
