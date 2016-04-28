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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HttpBinHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(
            HttpBinHandler.class);

    @Override
    public void handle(String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse servletResponse)
            throws IOException {
        logger.trace("request: {}", request);
        for (String headerName : Collections.list(
                request.getHeaderNames())) {
            logger.trace("header: {}: {}", headerName,
                    request.getHeader(headerName));
        }
        String method = request.getMethod();
        String uri = request.getRequestURI();
        try (InputStream is = request.getInputStream();
             OutputStream os = servletResponse.getOutputStream()) {
            Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            if (method.equals("GET") && uri.startsWith("/status/")) {
                ByteStreams.copy(is, ByteStreams.nullOutputStream());
                int status = Integer.parseInt(uri.substring(
                        "/status/".length()));
                servletResponse.setStatus(status);
                baseRequest.setHandled(true);
                return;
            } else if (method.equals("GET") && uri.equals("/get")) {
                ByteStreams.copy(is, ByteStreams.nullOutputStream());
                // TODO: return JSON blob of request
                String content = "Hello, world!";
                servletResponse.setContentLength(content.length());
                servletResponse.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                writer.write(content);
                writer.flush();
                return;
            } else if (method.equals("POST") && uri.equals("/post")) {
                ByteStreams.copy(is, os);
                servletResponse.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                return;
            } else if (method.equals("PUT") && uri.equals("/put")) {
                ByteStreams.copy(is, os);
                servletResponse.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                return;
            } else if (method.equals("GET") &&
                    uri.equals("/response-headers")) {
                ByteStreams.copy(is, ByteStreams.nullOutputStream());
                for (String paramName : Collections.list(
                        request.getParameterNames())) {
                    servletResponse.addHeader(paramName, request.getParameter(
                            paramName));
                }
                servletResponse.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                return;
            }
            servletResponse.setStatus(501);
            baseRequest.setHandled(true);
        }
    }
}
