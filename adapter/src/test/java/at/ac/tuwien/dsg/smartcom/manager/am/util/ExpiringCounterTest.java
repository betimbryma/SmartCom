package at.ac.tuwien.dsg.smartcom.manager.am.util;

import at.ac.tuwien.dsg.smartcom.utils.ExpiringCounter;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ExpiringCounterTest {

    @Test
    public void testIncrease() throws Exception {
        ExpiringCounter counter = new ExpiringCounter(5, TimeUnit.SECONDS);
        counter.increase();
        counter.increase();
        counter.increase();
        counter.increase();
        counter.increase();

        synchronized (this) {
            wait(6000);
        }

        assertEquals(0, counter.getCounter());
    }
}