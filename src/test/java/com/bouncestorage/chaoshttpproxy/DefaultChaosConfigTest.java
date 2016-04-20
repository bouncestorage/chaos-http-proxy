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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Properties;

import org.junit.Test;

public class DefaultChaosConfigTest {

    @Test
    public final void testPropertyFromStream() throws Exception {
        final PipedOutputStream os = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream(os);
        final Properties properties = new Properties();
        properties.put(Failure.HTTP_301.toString(), "2");
        properties.put(Failure.SUCCESS.toString(), "3");
        properties.put("random_key", "random_value");
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    properties.store(os, "no comment");
                    os.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();
        DefaultChaosConfig config = new DefaultChaosConfig(is);

        Properties configProperties = config.getProperties();

        assertFailureMatch(properties, Failure.HTTP_301, configProperties);

        assertFailureMatch(properties, Failure.SUCCESS, configProperties);

        assertThat(configProperties.containsKey("random_key")).as("extra keys")
                .isEqualTo(false);

    }

    private void assertFailureMatch(final Properties properties,
            Failure failure, Properties configProperties) {
        assertThat(configProperties.containsKey(failure.toString()))
                .as("has key" + failure.toString()).isEqualTo(true);
        assertThat(configProperties.get(failure.toString()))
                .as("has value" + failure.toString())
                .isEqualTo(properties.getProperty(failure.toString()));
    }

}
