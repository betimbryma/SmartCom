package at.ac.tuwien.dsg.smartcom.utils;

import at.ac.tuwien.dsg.smartcom.adapters.RESTOutputAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public final class PropertiesLoader {
    private static final Logger log = LoggerFactory.getLogger(RESTOutputAdapter.class);

    /**
     * Returns the value of a property in a file specified by filename.
     * Returns null if there is no such file or property in the file.
     * @param filename of the properties file
     * @param property that is requested
     * @return the value of the property or null
     */
    public static String getProperty(String filename, String property) {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            //get file from root directory, if there is no such file, get it from the classpath
            try {
                input = new FileInputStream(filename);
            } catch (FileNotFoundException e) {
                input = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
                if (input == null) {
                    input = Thread.currentThread().getContextClassLoader().getResourceAsStream("/"+filename);
                    if (input == null) {
                        log.error("Could not load file '{}'! It is neither available in the root directory nor in the classpath!", filename);
                        return null;
                    }
                }
            }

            prop.load(input);

            return (String) prop.get(property);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
