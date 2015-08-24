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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;
import com.google.common.util.concurrent.Uninterruptibles;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ChaosHttpProxyHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(
            ChaosHttpProxyHandler.class);
    private final HttpClient client;
    // TODO: AtomicReference?
    private Supplier<Failure> supplier;
    private final Map<String, String> redirects = new ConcurrentHashMap<>();

    ChaosHttpProxyHandler(HttpClient client, Supplier<Failure> supplier) {
        this.client = requireNonNull(client);
        this.supplier = requireNonNull(supplier);
    }

    @Override
    public void handle(String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse servletResponse)
            throws IOException {
        // CONNECT is not supported pending implementation of MITM HTTPS
        if (request.getMethod().equals("CONNECT")) {
            logger.debug("CONNECT is not supported");
            servletResponse.sendError(
                    HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        Failure failure = supplier.get();
        logger.debug("request: {}", request);
        logger.debug("Failure: {}", failure);
        try (InputStream is = request.getInputStream();
             OutputStream os = servletResponse.getOutputStream()) {
            StringBuilder uri = new StringBuilder("http://")
                    .append(request.getHeader(HttpHeaders.HOST))
                    .append(request.getRequestURI());
            String separator = "?";
            for (String parameterName :
                    Collections.list(request.getParameterNames())) {
                uri.append(separator).append(parameterName);
                // TODO: duplicate parameters?
                String parameterValue = request.getParameter(parameterName);
                if (parameterValue != null) {
                    uri.append("=").append(parameterValue);
                }
                separator = "&";
            }
            String redirectedUri = redirects.get(uri.toString());
            if (redirectedUri != null) {
                // TODO: parameters
                uri = new StringBuilder(redirectedUri);
                logger.debug("redirected uri: {}", uri);
            }

            switch (failure) {
            case HTTP_301:
            case HTTP_302:
            case HTTP_303:
            case HTTP_307:
            case HTTP_308:
                servletResponse.setStatus(failure.getResponseCode());
                String oldUri = "http://" +
                        request.getHeader(HttpHeaders.HOST) +
                        request.getRequestURI();
                String newUri = "http://" +
                        request.getHeader(HttpHeaders.HOST) +
                        "/" + UUID.randomUUID().toString();
                redirects.put(newUri, oldUri);
                servletResponse.addHeader(HttpHeaders.LOCATION, newUri);
                return;
            case HTTP_408:
            case HTTP_500:
            case HTTP_503:
            case HTTP_504:
                servletResponse.setStatus(failure.getResponseCode());
                return;
            case TIMEOUT:
                Uninterruptibles.sleepUninterruptibly(Long.MAX_VALUE,
                        TimeUnit.DAYS);
                return;
            default:
                break;
            }

            InputStreamResponseListener listener =
                    new InputStreamResponseListener();
            org.eclipse.jetty.client.api.Request clientRequest = client
                    .newRequest(uri.toString())
                    .method(request.getMethod());
            long userContentLength = -1;
            for (String headerName :
                    Collections.list(request.getHeaderNames())) {
                if (headerName.equalsIgnoreCase(HttpHeaders.EXPECT) ||
                        headerName.equalsIgnoreCase("Proxy-Connection")) {
                    continue;
                }
                String headerValue = request.getHeader(headerName);
                logger.trace("{}: {}", headerName, headerValue);

                if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_MD5) &&
                        failure == Failure.CORRUPT_REQUEST_CONTENT_MD5) {
                    headerValue = headerValue.toUpperCase();
                }
                if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                    userContentLength = Long.valueOf(headerValue);
                }
                clientRequest.header(headerName, headerValue);
            }

            // Work around Jetty bug that strips Content-Length
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=475613.
            final long length = userContentLength;
            clientRequest.content(new InputStreamContentProvider(is) {
                    @Override
                    public long getLength() {
                        return length != -1 ? length : super.getLength();
                    }
                });
            clientRequest.send(listener);

            Response response;
            try {
                response = listener.get(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException |
                    TimeoutException e) {
                throw new IOException(e);
            }
            int status = response.getStatus();
            logger.trace("status: {}", status);
            servletResponse.setStatus(status);
            List<HttpField> headers = Lists.newArrayList(
                    response.getHeaders());
            if (failure == Failure.REORDER_HEADERS) {
                Collections.shuffle(headers);
            }
            for (HttpField field : headers) {
                String header = field.getName();
                String value = field.getValue();
                logger.trace("header: {}: {}", header, value);
                switch (failure) {
                case CHANGE_HEADER_CASE:
                    // TODO: randomly change between upper- and lower-case
                    header = header.toUpperCase();
                    break;
                case CORRUPT_RESPONSE_CONTENT_MD5:
                    if (header.equals(HttpHeaders.CONTENT_MD5)) {
                        value = BaseEncoding.base64().encode(
                                new byte[Hashing.md5().bits() / 8]);
                    }
                    break;
                default:
                    break;
                }
                servletResponse.addHeader(header, value);
            }
            try (InputStream responseContent = listener.getInputStream()) {
                switch (failure) {
                case PARTIAL_DATA:
                    byte[] array = new byte[1024];
                    int count = responseContent.read(array);
                    if (count != -1) {
                        // TODO: randomly read n - 1 bytes
                        os.write(array, 0, count / 2);
                        os.flush();
                    }
                    return;
                case SLOW_RESPONSE:
                    for (int i = 0; i < 10; ++i) {
                        int ch = responseContent.read();
                        if (ch == -1) {
                            break;
                        }
                        os.write(ch);
                        os.flush();
                        Uninterruptibles.sleepUninterruptibly(1,
                                TimeUnit.SECONDS);
                    }
                    break;
                default:
                    break;
                }
                ByteStreams.copy(responseContent, os);
            }
        }
    }

    @VisibleForTesting
    void setFailureSupplier(Supplier<Failure> supplier) {
        this.supplier = requireNonNull(supplier);
    }
}
