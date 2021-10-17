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

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.annotations.VisibleForTesting;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;

public final class ChaosHttpProxy {
    private final HttpClient client;
    private final Server server;
    private final ChaosHttpProxyHandler handler;
    private ChaosConfig config;

    // TODO: authentication
    public ChaosHttpProxy(URI endpoint, ChaosConfig config)
            throws Exception {
        setChaosConfig(config);

        Supplier<Failure> supplier = new RandomFailureSupplier(
                config.getFailures());

        requireNonNull(endpoint);

        client = new HttpClient();

        server = new Server();
        HttpConnectionFactory httpConnectionFactory =
                new HttpConnectionFactory();
        // TODO: SSL
        ServerConnector connector = new ServerConnector(server,
                httpConnectionFactory);
        connector.setHost(endpoint.getHost());
        connector.setPort(endpoint.getPort());
        server.addConnector(connector);
        this.handler = new ChaosHttpProxyHandler(client, supplier);
        HandlerList handlers = new HandlerList();
        handlers.addHandler(new ChaosApiHandler(this, handler));
        handlers.addHandler(handler);
        server.setHandler(handlers);
    }

    protected ChaosConfig getChaosConfig() {
        return config;
    }

    void setChaosConfig(ChaosConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    public void start() throws Exception {
        server.start();
        client.start();
    }

    public void stop() throws Exception {
        client.stop();
        server.stop();
    }

    public int getPort() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    @VisibleForTesting
    void setFailureSupplier(Supplier<Failure> supplier) {
        handler.setFailureSupplier(supplier);
    }
}
