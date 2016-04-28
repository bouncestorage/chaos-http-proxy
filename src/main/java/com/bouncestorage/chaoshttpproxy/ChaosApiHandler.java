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
import java.io.OutputStream;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

final class ChaosApiHandler extends AbstractHandler {
    private final ChaosHttpProxy chaosHttpProxy;
    private final ChaosHttpProxyHandler chaosHttpProxyHandler;

    ChaosApiHandler(ChaosHttpProxy chaosHttpProxy,
            ChaosHttpProxyHandler chaosHttpProxyHandler) {
        this.chaosHttpProxy = Objects.requireNonNull(chaosHttpProxy);
        this.chaosHttpProxyHandler = Objects.requireNonNull(
                chaosHttpProxyHandler);
    }

    @Override
    public void handle(String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (target.equals("/chaos/api")) {
            baseRequest.setHandled(true);
            response.setContentType("text/plain");

            try (InputStream is = request.getInputStream();
                    OutputStream os = response.getOutputStream()) {
                switch (HttpMethod.valueOf(request.getMethod())) {
                case POST:
                    ChaosConfig config;
                    try {
                        config = ChaosConfig.loadFromPropertyStream(is);
                    } catch (IllegalArgumentException iae) {
                        response.getWriter().println(iae.getMessage());
                        response.setStatus(
                                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        return;
                    }
                    chaosHttpProxy.setChaosConfig(config);
                    chaosHttpProxyHandler.setFailureSupplier(
                            new RandomFailureSupplier(config.getFailures()));
                    break;
                case GET:
                    response.setStatus(HttpServletResponse.SC_OK);
                    chaosHttpProxy.getChaosConfig().getProperties().store(
                            os, "Output via api");
                    break;
                default:
                    response.setStatus(
                            HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                }
            }
        }
    }
}
