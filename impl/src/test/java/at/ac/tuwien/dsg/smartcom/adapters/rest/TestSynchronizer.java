package at.ac.tuwien.dsg.smartcom.adapters.rest;

import java.util.concurrent.CountDownLatch;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public final class TestSynchronizer {

    private static CountDownLatch latch;

    public static void initSynchronizer(int counter) {
        latch = new CountDownLatch(counter);
    }

    public static void await() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void countDown() {
        latch.countDown();
    }
}
