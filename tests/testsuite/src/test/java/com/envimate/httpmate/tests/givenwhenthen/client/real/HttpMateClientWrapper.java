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

package com.envimate.httpmate.tests.givenwhenthen.client.real;

import com.envimate.httpmate.client.HttpClientRequestBuilder;
import com.envimate.httpmate.client.HttpMateClient;
import com.envimate.httpmate.client.SimpleHttpResponseObject;
import com.envimate.httpmate.client.body.multipart.Part;
import com.envimate.httpmate.client.issuer.real.Protocol;
import com.envimate.httpmate.tests.givenwhenthen.builders.MultipartElement;
import com.envimate.httpmate.tests.givenwhenthen.client.HttpClientResponse;
import com.envimate.httpmate.tests.givenwhenthen.client.HttpClientWrapper;
import com.envimate.httpmate.tests.givenwhenthen.deploy.Deployment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.envimate.httpmate.client.HttpClientRequest.aRequest;
import static com.envimate.httpmate.client.HttpMateClient.aHttpMateClientBypassingRequestsDirectlyTo;
import static com.envimate.httpmate.client.body.multipart.Part.aPartWithTheControlName;
import static com.envimate.httpmate.client.issuer.real.Protocol.valueOf;
import static com.envimate.httpmate.tests.givenwhenthen.client.HttpClientResponse.httpClientResponse;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMateClientWrapper implements HttpClientWrapper {

    private final HttpMateClient client;

    static HttpClientWrapper realHttpMateClientWithConnectionReueWrapper(final Deployment deployment) {
        final Protocol protocol = valueOf(deployment.protocol().toUpperCase());
        final HttpMateClient client = HttpMateClient.aHttpMateClientThatReusesConnectionsForTheHost(deployment.hostname())
                .withThePort(deployment.port())
                .viaTheProtocol(protocol)
                .withBasePath(deployment.basePath())
                .build();
        return new HttpMateClientWrapper(client);
    }

    static HttpClientWrapper realHttpMateClientWrapper(final Deployment deployment) {
        final Protocol protocol = valueOf(deployment.protocol().toUpperCase());
        final HttpMateClient client = HttpMateClient.aHttpMateClientForTheHost(deployment.hostname())
                .withThePort(deployment.port())
                .viaTheProtocol(protocol)
                .withBasePath(deployment.basePath())
                .build();
        return new HttpMateClientWrapper(client);
    }

    static HttpClientWrapper bypassingHttpMateClientWrapper(final Deployment deployment) {
        final HttpMateClient client = aHttpMateClientBypassingRequestsDirectlyTo(deployment.httpMate())
                .build();
        return new HttpMateClientWrapper(client);
    }

    @Override
    public HttpClientResponse issueRequestWithoutBody(final String path,
                                                      final String method,
                                                      final Map<String, String> headers) {
        return issueRequest(path, method, headers, builder -> {
        });
    }

    @Override
    public HttpClientResponse issueRequestWithStringBody(final String path,
                                                         final String method,
                                                         final Map<String, String> headers,
                                                         final String body) {
        return issueRequest(path, method, headers, bodyStage -> bodyStage.withTheBody(body));
    }

    @Override
    public HttpClientResponse issueRequestWithMultipartBody(final String path,
                                                            final String method,
                                                            final Map<String, String> headers,
                                                            final List<MultipartElement> parts) {
        return issueRequest(path, method, headers, builder -> {
            final Part[] partsArray = parts.stream()
                    .map(part -> aPartWithTheControlName(part.controlName())
                            .withTheFileName(part.fileName().orElse(null))
                            .withTheContent(part.content()))
                    .toArray(Part[]::new);
            builder.withAMultipartBodyWithTheParts(partsArray);
        });
    }

    private HttpClientResponse issueRequest(final String path,
                                            final String method,
                                            final Map<String, String> headers,
                                            final Consumer<HttpClientRequestBuilder<SimpleHttpResponseObject>> bodyAppender) {
        final HttpClientRequestBuilder<SimpleHttpResponseObject> requestBuilder = aRequest(method, path);
        bodyAppender.accept(requestBuilder);
        headers.forEach(requestBuilder::withHeader);
        final SimpleHttpResponseObject response = this.client.issue(requestBuilder);
        return httpClientResponse(response.getStatusCode(), response.getHeaders(), response.getBody());
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
