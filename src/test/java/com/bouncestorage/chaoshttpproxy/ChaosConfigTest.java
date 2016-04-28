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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ChaosConfigTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public final void testValidProperty() throws Exception {
        Properties properties = new Properties();
        properties.put(Failure.HTTP_301.toPropertyName(), "1");

        ByteArrayInputStream is = propertiesToInputStream(properties);
        ChaosConfig config = ChaosConfig.loadFromPropertyStream(is);

        Properties configProperties = config.getProperties();
        assertFailureMatch(properties, Failure.HTTP_301, configProperties);
    }

    @Test
    public final void testInvalidProperty() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("random_key", "exception");

        ByteArrayInputStream is = propertiesToInputStream(properties);
        thrown.expect(IllegalArgumentException.class);
        ChaosConfig.loadFromPropertyStream(is);
    }

    private static ByteArrayInputStream propertiesToInputStream(
            Properties properties) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        properties.store(os, "no comment");
        return new ByteArrayInputStream(os.toByteArray());
    }

    private static void assertFailureMatch(Properties properties,
            Failure failure, Properties configProperties) {
        String name = failure.toPropertyName();
        assertThat(configProperties.getProperty(name)).as("has value" + name)
                .isEqualTo(properties.getProperty(name));
    }
}
