package at.ac.tuwien.dsg.smartcom.statistic;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class StatisticBean {

    private AtomicInteger sendingRequest = new AtomicInteger(0);
    private AtomicInteger internalSendingRequest = new AtomicInteger(0);
    private AtomicInteger externalSendingRequest = new AtomicInteger(0);
    private AtomicInteger componentMessage = new AtomicInteger(0);
    private AtomicInteger collectiveMessage = new AtomicInteger(0);
    private AtomicInteger peerMessage = new AtomicInteger(0);
    private AtomicInteger callbackMessage = new AtomicInteger(0);
    private AtomicInteger logMessage = new AtomicInteger(0);

    public void sendingRequestReceived() {
        sendingRequest.incrementAndGet();
    }

    public void internalMessageSendingRequest() {
        internalSendingRequest.incrementAndGet();
    }

    public void externalMessageSendingRequest(){
        externalSendingRequest.incrementAndGet();
    }

    public void componentMessageSendingRequest(){
        componentMessage.incrementAndGet();
    }

    public void collectiveMessageSendingRequest(){
        collectiveMessage.incrementAndGet();
    }

    public void peerMessageSendingRequest(){
        peerMessage.incrementAndGet();
    }

    public void callbackCalled(){
        callbackMessage.incrementAndGet();
    }

    public void logRequest(){
        logMessage.incrementAndGet();
    }

    public Statistic getStatistic() {
        return new Statistic(
                sendingRequest.get(),
                internalSendingRequest.get(),
                externalSendingRequest.get(),
                componentMessage.get(),
                collectiveMessage.get(),
                peerMessage.get(),
                callbackMessage.get(),
                logMessage.get(),
                new BrokerStatistic(
                    inputMessageReceived.get(),
                    inputMessagePublished.get(),
                    logMessageReceived.get(),
                    logMessagePublished.get(),
                    controlMessageReceived.get(),
                    controlMessagePublished.get(),
                    outputMessageReceived.get(),
                    outputMessagePublished.get(),
                    requestMessageReceived.get(),
                    requestMessagePublished.get()
                )

        );
    }

    /* ##################
     * #                #
     * # BROKER SECTION #
     * #                #
     * ##################
     */

    private AtomicInteger inputMessageReceived = new AtomicInteger(0);
    private AtomicInteger inputMessagePublished = new AtomicInteger(0);

    private AtomicInteger logMessageReceived = new AtomicInteger(0);
    private AtomicInteger logMessagePublished = new AtomicInteger(0);

    private AtomicInteger controlMessageReceived = new AtomicInteger(0);
    private AtomicInteger controlMessagePublished = new AtomicInteger(0);

    private AtomicInteger outputMessageReceived = new AtomicInteger(0);
    private AtomicInteger outputMessagePublished = new AtomicInteger(0);

    private AtomicInteger requestMessageReceived = new AtomicInteger(0);
    private AtomicInteger requestMessagePublished = new AtomicInteger(0);

    public void brokerPublishInput() {
        inputMessagePublished.incrementAndGet();
    }

    public void inputReceived(){
        inputMessageReceived.incrementAndGet();
    }

    public void brokerPublishLog() {
        logMessagePublished.incrementAndGet();
    }

    public void logReceived() {
        logMessageReceived.incrementAndGet();
    }

    public void brokerPublishControl() {
        controlMessagePublished.incrementAndGet();
    }

    public void controlReceived() {
        controlMessageReceived.incrementAndGet();
    }

    public void brokerPublishOutput() {
        outputMessagePublished.incrementAndGet();
    }

    public void outputReceived() {
        outputMessageReceived.incrementAndGet();
    }

    public void brokerPublishRequest() {
        requestMessagePublished.incrementAndGet();
    }

    public void requestReceived() {
        requestMessageReceived.incrementAndGet();
    }

}
