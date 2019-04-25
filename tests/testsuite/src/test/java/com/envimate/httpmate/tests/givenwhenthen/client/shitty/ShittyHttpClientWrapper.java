/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.envimate.httpmate.tests.givenwhenthen.client.shitty;

import com.envimate.httpmate.tests.givenwhenthen.client.HttpClientResponse;
import com.envimate.httpmate.tests.givenwhenthen.builders.MultipartElement;
import com.envimate.httpmate.tests.givenwhenthen.client.HttpClientWrapper;
import com.envimate.httpmate.tests.givenwhenthen.deploy.Deployment;
import com.envimate.httpmate.util.Streams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.*;
import org.apache.http.ssl.SSLContexts;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.envimate.httpmate.tests.givenwhenthen.client.HttpClientResponse.httpClientResponse;
import static com.envimate.httpmate.util.Streams.inputStreamToString;
import static java.util.Arrays.stream;
import static org.apache.http.protocol.HttpProcessorBuilder.create;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShittyHttpClientWrapper implements HttpClientWrapper {
    private static final String MULTIPART_BOUNDARY = "abcdefggggg";

    private final Deployment deployment;

    static HttpClientWrapper shittyHttpClientWrapper(final Deployment deployment) {
        return new ShittyHttpClientWrapper(deployment);
    }

    @Override
    public HttpClientResponse issueRequestWithoutBody(final String path,
                                                      final String method,
                                                      final Map<String, String> headers) {
        return issueRequest(path, method, headers, request -> {
        });
    }

    @Override
    public HttpClientResponse issueRequestWithStringBody(final String path,
                                                         final String method,
                                                         final Map<String, String> headers,
                                                         final String body) {
        return issueRequest(path, method, headers, request -> {
            try {
                request.setEntity(new StringEntity(body));
            } catch (final UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public HttpClientResponse issueRequestWithMultipartBody(final String path,
                                                            final String method,
                                                            final Map<String, String> headers,
                                                            final List<MultipartElement> parts) {
        return issueRequest(path, method, headers, request -> {
            final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder
                    .create().setBoundary(MULTIPART_BOUNDARY);
            for (final MultipartElement part : parts) {
                if (part.fileName().isPresent()) {
                    final InputStream inputStream = Streams.stringToInputStream(part.content());
                    final InputStreamBody body = new InputStreamBody(inputStream, part.fileName().get());
                    multipartEntityBuilder.addPart(new FormBodyPart(part.controlName(), body));
                } else {
                    try {
                        final StringBody stringBody = new StringBody(part.content());
                        multipartEntityBuilder.addPart(new FormBodyPart(part.controlName(), stringBody));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            request.setEntity(multipartEntityBuilder.build());
        });
    }

    private HttpClientResponse issueRequest(final String path,
                                            final String method,
                                            final Map<String, String> headers,
                                            final Consumer<HttpEntityEnclosingRequest> bodyAppender) {
        final String url = appendPathToUrl(deployment.baseUrl(), path);
        final HttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest(method, url);
        headers.forEach(request::addHeader);
        bodyAppender.accept(request);
        final DefaultBHttpClientConnection connection = new DefaultBHttpClientConnection(8 * 1024);
        try {
            final Socket socket = createSocket(deployment);
            connection.bind(socket);
            final HttpProcessor httpProcessor = create()
                    .add(new RequestContent())
                    .add(new RequestTargetHost())
                    .build();
            final HttpCoreContext context = HttpCoreContext.create();
            context.setTargetHost(new HttpHost(deployment.hostname()));
            httpProcessor.process(request, context);
            final HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
            final HttpResponse response = httpexecutor.execute(request, connection, context);
            final int statusCode = response.getStatusLine().getStatusCode();
            final Map<String, String> responseHeaders = new HashMap<>();
            stream(response.getAllHeaders())
                    .forEach(header -> responseHeaders.put(header.getName().toLowerCase(), header.getValue()));
            final String responseBody = inputStreamToString(response.getEntity().getContent());
            return httpClientResponse(statusCode, responseHeaders, responseBody);
        } catch (final IOException | HttpException e) {
            throw new RuntimeException(e);
        }
    }

    private static String appendPathToUrl(final String url, final String path) {
        final String normalizedUrl;
        if (url.endsWith("/")) {
            normalizedUrl = url.substring(0, url.length() - 1);
        } else {
            normalizedUrl = url;
        }

        final String normalizedPath;
        if (path.startsWith("/")) {
            normalizedPath = path.substring(1);
        } else {
            normalizedPath = path;
        }
        return normalizedUrl + "/" + normalizedPath;
    }

    private static Socket createSocket(final Deployment deployment) {
        try {
            if (deployment.protocol().equals("http")) {
                return new Socket(deployment.hostname(), deployment.port());
            }
            final SSLContext sslcontext = SSLContexts.createSystemDefault();
            final SocketFactory socketFactory = sslcontext.getSocketFactory();

            final SSLSocket socket = (SSLSocket) socketFactory.createSocket(deployment.hostname(), deployment.port());
            // Enforce TLS and disable SSL
            socket.setEnabledProtocols(new String[]{
                    "TLSv1",
                    "TLSv1.1",
                    "TLSv1.2"});
            // Enforce strong ciphers
            socket.setEnabledCipherSuites(new String[]{
                    "TLS_RSA_WITH_AES_256_CBC_SHA",
                    "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
                    "TLS_DHE_DSS_WITH_AES_256_CBC_SHA"});
            return socket;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
