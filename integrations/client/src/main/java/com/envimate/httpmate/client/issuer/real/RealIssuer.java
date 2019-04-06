/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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

package com.envimate.httpmate.client.issuer.real;

import com.envimate.httpmate.client.BasePath;
import com.envimate.httpmate.client.HttpClientRequest;
import com.envimate.httpmate.client.RawClientResponse;
import com.envimate.httpmate.client.issuer.Issuer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.http.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.envimate.httpmate.client.RawClientResponse.rawClientResponse;
import static com.envimate.httpmate.client.issuer.real.NormalConnectionFactory.normalConnectionFactory;
import static com.envimate.httpmate.client.issuer.real.PooledConnectionFactory.pooledConnectionFactory;
import static com.envimate.httpmate.client.issuer.real.Endpoint.endpoint;
import static java.util.Arrays.stream;
import static org.apache.http.protocol.HttpProcessorBuilder.create;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RealIssuer implements Issuer {
    private final Endpoint endpoint;
    private final ConnectionFactory connectionFactory;

    public static Issuer realIssuer(final Protocol protocol,
                                    final String host,
                                    final int port,
                                    final BasePath basePath) {
        return new RealIssuer(endpoint(protocol, host, port, basePath), normalConnectionFactory());
    }

    public static Issuer realIssuerWithConnectionReuse(final Protocol protocol,
                                                       final String host,
                                                       final int port,
                                                       final BasePath basePath) {
        return new RealIssuer(endpoint(protocol, host, port, basePath), pooledConnectionFactory());
    }

    @Override
    public <T> T issue(final HttpClientRequest<T> request,
                       final BasePath basePath,
                       final Function<RawClientResponse, T> responseMapper) {
        final String path = request.path();
        final Map<String, String> queryParameters = request.queryParameters();
        final String url = this.endpoint.toUrl(path, queryParameters);
        final String method = request.method();
        final HttpEntityEnclosingRequest lowLevelRequest = new BasicHttpEntityEnclosingRequest(method, url);
        request.headers().forEach(lowLevelRequest::addHeader);
        final InputStream requestBody = request.body();
        final InputStreamEntity entity = new InputStreamEntity(requestBody);
        lowLevelRequest.setEntity(entity);
        try (Connection connection = connectionFactory.getConnectionTo(endpoint)) {
            final HttpProcessor httpProcessor = create()
                    .add(new RequestContent())
                    .add(new RequestTargetHost())
                    .build();
            final HttpCoreContext context = HttpCoreContext.create();
            context.setTargetHost(new HttpHost(this.endpoint.host()));
            httpProcessor.process(lowLevelRequest, context);
            final HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
            final HttpClientConnection connectionObject = connection.connectionObject();
            final HttpResponse response = httpexecutor.execute(lowLevelRequest, connectionObject, context);
            final int statusCode = response.getStatusLine().getStatusCode();
            final Map<String, String> headers = new HashMap<>();
            stream(response.getAllHeaders())
                    .forEach(header -> headers.put(header.getName().toLowerCase(), header.getValue()));
            final InputStream body = response.getEntity().getContent();
            final RawClientResponse rawClientResponse = rawClientResponse(statusCode, headers, body);
            return responseMapper.apply(rawClientResponse);
        } catch (final IOException | HttpException e) {
            throw new RuntimeException(e);
        }
    }
}
