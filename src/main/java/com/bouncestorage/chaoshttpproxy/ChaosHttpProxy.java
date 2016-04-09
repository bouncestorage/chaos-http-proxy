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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public final class ChaosHttpProxy {
    private final HttpClient client;
    private final Server server;
    private final ChaosHttpProxyHandler handler;

    // TODO: authentication
    public ChaosHttpProxy(URI endpoint, Supplier<Failure> supplier)
            throws Exception {
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
        handlers.addHandler(new AbstractHandler(){

			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request,
					HttpServletResponse response) throws IOException, ServletException {
				if(request.getMethod().equals("POST") && target.equals("/chaos/api")){
					
					BufferedReader r = new BufferedReader(new InputStreamReader(request.getInputStream()));
					Gson gson = new Gson();
					Type type = new TypeToken<Map<String, Integer>>(){}.getType();
					Map<String, Integer> failureConfig = gson.fromJson(r, type);
					List<Failure> failures = new ArrayList<Failure>();
					for(String key : failureConfig.keySet()){
						Failure failure = Failure.valueOf(key.toUpperCase());
						int size = failureConfig.get(key);
						for(int i= 0; i < size; i++){
							failures.add(failure);
						}
					}
					
					handler.setFailureSupplier(new RandomFailureSupplier(failures));
					
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					response.getWriter().close();
				}
				
			}});
        handlers.addHandler(handler);
        server.setHandler(handlers);
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
