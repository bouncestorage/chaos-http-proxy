/*
 * Copyright 2015 Bounce Storage, Inc. <info@bouncestorage.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bouncestorage.chaoshttpproxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DefaultChaosConfig implements ChaosConfig {

    private List<Failure> failures;

    private Properties properties;

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

    /* (non-Javadoc)
     * @see com.bouncestorage.chaoshttpproxy.ChaosConfig_#getFailures()
     */
    @Override
    public final List<Failure> getFailures() {
        return failures;
    }

    /* (non-Javadoc)
     * @see com.bouncestorage.chaoshttpproxy.ChaosConfig_#getProperties()
     */
    @Override
    public final Properties getProperties() {
        return properties;
    }


}
