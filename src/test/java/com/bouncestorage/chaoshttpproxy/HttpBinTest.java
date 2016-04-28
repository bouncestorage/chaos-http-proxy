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

import java.net.URI;
import java.util.Random;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpBinTest {
    private static final Logger logger = LoggerFactory.getLogger(
            HttpBinTest.class);

    private URI httpBinEndpoint = URI.create("http://127.0.0.1:0");

    private HttpBin httpBin;
    private HttpClient client;

    @Before
    public void setUp() throws Exception {
        httpBin = new HttpBin(httpBinEndpoint);
        httpBin.start();

        // reset endpoint to handle zero port
        httpBinEndpoint = new URI(httpBinEndpoint.getScheme(),
                httpBinEndpoint.getUserInfo(), httpBinEndpoint.getHost(),
                httpBin.getPort(), httpBinEndpoint.getPath(),
                httpBinEndpoint.getQuery(), httpBinEndpoint.getFragment());
        logger.debug("HttpBin listening on {}", httpBinEndpoint);

        client = new HttpClient();
        client.start();
    }

    @After
    public void tearDown() throws Exception {
        if (client != null) {
            client.stop();
        }
        if (httpBin != null) {
            httpBin.stop();
        }
    }

    @Test
    public void testPostData() throws Exception {
        byte[] input = new byte[1024];
        new Random().nextBytes(input);
        ContentResponse response = client.POST(httpBinEndpoint + "/post")
                .content(new BytesContentProvider(input))
                .send();
        assertThat(response.getStatus()).as("status").isEqualTo(200);
        assertThat(response.getContent()).isEqualTo(input);
    }

    @Test
    public void testPutData() throws Exception {
        byte[] input = new byte[1024];
        new Random().nextBytes(input);
        ContentResponse response = client.newRequest(httpBinEndpoint + "/put")
                .method("PUT")
                .content(new BytesContentProvider(input))
                .send();
        assertThat(response.getStatus()).as("status").isEqualTo(200);
        assertThat(response.getContent()).isEqualTo(input);
    }
}
