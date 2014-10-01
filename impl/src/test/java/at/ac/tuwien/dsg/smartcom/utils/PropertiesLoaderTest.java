package at.ac.tuwien.dsg.smartcom.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PropertiesLoaderTest {

    @Test
    public void testGetProperty() throws Exception {
        assertEquals("true", PropertiesLoader.getProperty("PropertiesLoaderTest.properties", "test"));
        assertEquals("false", PropertiesLoader.getProperty("PropertiesLoaderTest.properties", "test2"));

        //Property does not exist
        assertNull(PropertiesLoader.getProperty("PropertiesLoaderTest.properties", "test3"));

        //file does not exist
        assertNull(PropertiesLoader.getProperty("NotExistingFile.properties", "test"));
    }
}