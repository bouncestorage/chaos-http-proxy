package com.bouncestorage.chaoshttpproxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DefaultChaosConfig implements ChaosConfig {
    
    private Properties properties;
    
    private List<Failure> failures;
    
    /* (non-Javadoc)
     * @see com.bouncestorage.chaoshttpproxy.ChaosConfig_#getProperties()
     */
    @Override
    public Properties getProperties() {
        return properties;
    }
    

    /* (non-Javadoc)
     * @see com.bouncestorage.chaoshttpproxy.ChaosConfig_#getFailures()
     */
    @Override
    public List<Failure> getFailures() {
        return failures;
    }

    public DefaultChaosConfig(InputStream is)
            throws IOException {
        properties = new Properties();
        properties.load(is);
        properties.putAll(System.getProperties());
        failures = new ArrayList<>();
        for (String propertyName : properties.stringPropertyNames()) {
            if (!propertyName.startsWith(Failure.CHAOS_CONFIG_STRING)) {
                properties.remove(propertyName);
                continue;
            }
            Failure failure;
            try {
                failure = Failure.fromPropertyKey(propertyName);
            } catch (IllegalArgumentException iae) {
                System.err.println("Invalid failure: " + propertyName);
                System.err.println("Valid failures:");
                for (Failure failure2 : Failure.values()) {
                    System.err.println(failure2.toString().toLowerCase());
                }
                System.exit(1);
                throw iae;
            }
            int occurrences = Integer.parseInt(properties.getProperty(
                    propertyName));
            for (int i = 0; i < occurrences; ++i) {
                failures.add(failure);
            }
        }

    }


}
