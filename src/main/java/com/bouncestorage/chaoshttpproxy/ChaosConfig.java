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

import com.google.common.collect.ImmutableList;

final class ChaosConfig {

    private final List<Failure> failures;

    private final Properties properties;

    public ChaosConfig(Properties properties) {
        List<Failure> failures = loadFailures(properties);
        this.properties = properties;
        this.failures = ImmutableList.copyOf(failures);
    }

    public static ChaosConfig loadFromPropertyStream(
            InputStream is)
            throws IOException {
        Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.load(is);

        return new ChaosConfig(properties);
    }

    private List<Failure> loadFailures(Properties properties) {
        List<Failure> failures = new ArrayList<>(properties.size());
        for (String propertyName : properties.stringPropertyNames()) {
            if (!propertyName.startsWith(Failure.CHAOS_CONFIG_PREFIX)) {
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
                throw new IllegalArgumentException("Unexpected Failure type " +
                        propertyName, iae);
            }
            int occurrences = Integer.parseInt(properties.getProperty(
                    propertyName));
            for (int i = 0; i < occurrences; ++i) {
                failures.add(failure);
            }
        }
        return failures;
    }

    public List<Failure> getFailures() {
        return failures;
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.putAll(this.properties);
        return properties;
    }


}
