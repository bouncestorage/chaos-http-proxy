/*
 * Copyright 2015-2016 Bounce Storage, Inc. <info@bouncestorage.com>
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
import java.util.List;
import java.util.Properties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

final class ChaosConfig {
    private final List<Failure> failures;

    ChaosConfig(Properties properties) {
        this.failures = loadFailures(properties);
    }

    static ChaosConfig loadFromPropertyStream(InputStream is)
            throws IOException {
        Properties properties = new Properties();
        properties.load(is);
        return new ChaosConfig(properties);
    }

    private static List<Failure> loadFailures(Properties properties) {
        ImmutableList.Builder<Failure> failures = ImmutableList.builder();
        for (String propertyName : properties.stringPropertyNames()) {
            Failure failure = Failure.fromPropertyName(propertyName);
            int occurrences = Integer.parseInt(properties.getProperty(
                    propertyName));
            for (int i = 0; i < occurrences; ++i) {
                failures.add(failure);
            }
        }
        return failures.build();
    }

    List<Failure> getFailures() {
        return failures;
    }

    Properties getProperties() {
        Properties properties = new Properties();
        for (Multiset.Entry<Failure> entry :
                ImmutableMultiset.copyOf(failures).entrySet()) {
            properties.setProperty(entry.getElement().toPropertyName(),
                    String.valueOf(entry.getCount()));
        }
        return properties;
    }
}
