package at.ac.tuwien.dsg.smartcom.scm.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.FeedbackAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.FeedbackPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.model.Message;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class FeedbackPullAdapterFacade implements FeedbackAdapterFacade {

    private final FeedbackPullAdapter adapter;

    public FeedbackPullAdapterFacade(FeedbackPullAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public Message checkForResponse() throws AdapterException {
        return adapter.pull();
    }

    @Override
    public void preDestroy() {
        //nothing to do here
    }

    @Override
    public FeedbackAdapter getAdapter() {
        return adapter;
    }
}
