package at.ac.tuwien.dsg.smartcom.utils;

import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PicoHelper {

    private final MutablePicoContainer pico;

    public PicoHelper() {
        pico = new PicoBuilder().withAnnotatedFieldInjection().withJavaEE5Lifecycle().withCaching().build();
    }

    public void start() {
        pico.start();
    }

    public void stop() {
        pico.stop();
        pico.dispose();
    }

    public void addComponent(Object component) {
        pico.as(Characteristics.CACHE).addComponent(component);
    }

    public void addComponent(Object name, Object instance) {
        pico.as(Characteristics.CACHE).addComponent(name, instance);
    }

    public <T> T getComponent(Class<T> componentType) {
        return pico.getComponent(componentType);
    }
}
