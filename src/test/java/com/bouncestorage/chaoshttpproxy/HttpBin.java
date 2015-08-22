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

import static java.util.Objects.requireNonNull;

import java.net.URI;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

/**
 * Reimplementation of HttpBin http://httpbin.org/ suitable for offline unit
 * tests.
 */
final class HttpBin {
    private final Server server;
    private final HttpBinHandler handler;

    public HttpBin(URI endpoint) throws Exception {
        this(endpoint, new HttpBinHandler());
    }

    public HttpBin(URI endpoint, HttpBinHandler handler) throws Exception {
        requireNonNull(endpoint);
        this.handler = handler;

        server = new Server();
        HttpConnectionFactory httpConnectionFactory =
                new HttpConnectionFactory();
        ServerConnector connector = new ServerConnector(server,
                httpConnectionFactory);
        connector.setHost(endpoint.getHost());
        connector.setPort(endpoint.getPort());
        server.addConnector(connector);
        server.setHandler(handler);
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public int getPort() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }
}
