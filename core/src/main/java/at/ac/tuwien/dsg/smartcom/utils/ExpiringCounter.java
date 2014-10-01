package at.ac.tuwien.dsg.smartcom.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class ExpiringCounter{

    private BlockingQueue<Long> entries;
    private final Thread removingThread;

    public ExpiringCounter(long duration, TimeUnit unit) {
        this.entries = new LinkedBlockingDeque<>();

        removingThread = new Thread(new ExpiringCounterRunnable(unit.toMillis(duration)), "ExpiringCounterDeleter");
        removingThread.setDaemon(true);
        removingThread.start();
    }

    public void destroy() {
        removingThread.interrupt();
    }

    public void increase() {
        this.entries.add(System.currentTimeMillis());
    }

    public void clear() {
        this.entries.clear();
    }

    public int getCounter() {
        return this.entries.size();
    }

    private class ExpiringCounterRunnable implements Runnable {

        private final long nanos;

        public ExpiringCounterRunnable(long nanos) {
            this.nanos = nanos;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    Long time = entries.take();

                    if (time != null) {
                        long currentTime = System.currentTimeMillis();
                        long timeOffset = time + nanos;

                        if (timeOffset > currentTime) {
                            synchronized (this) {
                                wait(timeOffset - currentTime);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
