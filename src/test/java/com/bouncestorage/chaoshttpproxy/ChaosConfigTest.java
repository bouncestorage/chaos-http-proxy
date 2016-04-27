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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Test;

public class ChaosConfigTest {

    @After
    //this method will remove all system properties set for chaos
    //so they don't muck with other tests.
    public final void cleanup() {
        for (String property : System.getProperties().stringPropertyNames()) {
            if (property.startsWith(Failure.CHAOS_CONFIG_PREFIX)) {
                System.getProperties().remove(property);
            }
        }
    }

    @Test
    public final void testPropertyFromStream() throws Exception {
        Properties properties = new Properties();
        properties.put(Failure.HTTP_301.toString(), "2");
        properties.put(Failure.SUCCESS.toString(), "3");
        properties.put("random_key", "random_value");

        //store the properties to an output stream so they may be read
        ByteArrayInputStream is = propertiesToInputStream(properties);

        ChaosConfig config = ChaosConfig.loadFromPropertyStream(
                is);

        Properties configProperties = config.getProperties();

        assertFailureMatch(properties, Failure.HTTP_301, configProperties);

        assertFailureMatch(properties, Failure.SUCCESS, configProperties);

        assertThat(configProperties.containsKey("random_key")).as("extra keys")
                .isEqualTo(false);

    }

    @Test
    public final void testPropertyFromSystem() throws IOException {
        Properties properties = new Properties();
        ByteArrayInputStream is = propertiesToInputStream(properties);

        System.setProperty(Failure.HTTP_302.toString(), "3");
        System.setProperty(Failure.HTTP_303.toString(), "1");
        System.setProperty("random_key", "better not show up");
        ChaosConfig config = ChaosConfig.loadFromPropertyStream(
                is);

        Properties configProperties = config.getProperties();

        assertFailureMatch(System.getProperties(), Failure.HTTP_302,
                configProperties);

        assertFailureMatch(System.getProperties(), Failure.HTTP_303,
                configProperties);

        assertThat(configProperties.containsKey("random_key")).as("extra keys")
                .isEqualTo(false);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testInvalidProperty() throws IOException {
        Properties properties = new Properties();
        ByteArrayInputStream is = propertiesToInputStream(properties);

        System.setProperty(Failure.HTTP_303.toString(), "1");
        System.setProperty(Failure.CHAOS_CONFIG_PREFIX + "random_key",
                "exception");
        ChaosConfig.loadFromPropertyStream(is);
    }

    private ByteArrayInputStream propertiesToInputStream(Properties properties)
            throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        properties.store(os, "no comment");

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        return is;
    }

    private void assertFailureMatch(final Properties properties,
            Failure failure, Properties configProperties) {
        assertThat(configProperties.getProperty(failure.toString()))
                .as("has value" + failure.toString())
                .isEqualTo(properties.getProperty(failure.toString()));
    }

}
