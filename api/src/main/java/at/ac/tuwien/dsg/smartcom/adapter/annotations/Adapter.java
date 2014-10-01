package at.ac.tuwien.dsg.smartcom.adapter.annotations;

import java.lang.annotation.*;

/**
 * This annotation will be used to indicate the properties
 * of an adapter that has been implemented.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Documented
@Retention(value= RetentionPolicy.RUNTIME)
@Target(value= ElementType.TYPE)
public @interface Adapter {

    /**
     * Indicates the name of the adapter (should be unique but can't be guaranteed)
     * @return the name of the adapter
     */
    String name();

    /**
     * Indicates if the adapter is stateful or stateless (default)
     * @return true if adapter is stateful
     */
    boolean stateful() default false;
}
