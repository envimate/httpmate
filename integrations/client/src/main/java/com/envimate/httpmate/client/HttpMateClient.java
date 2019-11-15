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

package com.envimate.httpmate.client;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.client.clientbuilder.PortStage;
import com.envimate.httpmate.client.issuer.Issuer;
import com.envimate.httpmate.filtermap.FilterMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.envimate.httpmate.client.HttpMateClientBuilder.clientBuilder;
import static com.envimate.httpmate.client.issuer.bypass.BypassIssuer.bypassIssuer;
import static com.envimate.httpmate.client.issuer.real.RealIssuer.realIssuer;
import static com.envimate.httpmate.client.issuer.real.RealIssuer.realIssuerWithConnectionReuse;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMateClient implements AutoCloseable {
    private final Issuer issuer;
    private final BasePath basePath;
    private final FilterMap<Class<?>, ClientResponseMapper<?>> responseMappers;

    static HttpMateClient httpMateClient(final Issuer issuer,
                                         final BasePath basePath,
                                         final FilterMap<Class<?>, ClientResponseMapper<?>> responseMappers) {
        validateNotNull(issuer, "issuer");
        validateNotNull(basePath, "basePath");
        validateNotNull(responseMappers, "responseMappers");
        return new HttpMateClient(issuer, basePath, responseMappers);
    }

    public static HttpMateClientBuilder aHttpMateClientBypassingRequestsDirectlyTo(final HttpMate httpMate) {
        validateNotNull(httpMate, "httpMate");
        final Issuer issuer = bypassIssuer(httpMate);
        return clientBuilder(basePath -> issuer);
    }

    public static PortStage aHttpMateClientForTheHost(final String host) {
        validateNotNullNorEmpty(host, "host");
        return port -> protocol -> {
            validateNotNull(protocol, "protocol");
            return clientBuilder(basePath -> realIssuer(protocol, host, port, basePath));
        };
    }

    public static PortStage aHttpMateClientThatReusesConnectionsForTheHost(final String host) {
        validateNotNullNorEmpty(host, "host");
        return port -> protocol -> {
            validateNotNull(protocol, "protocol");
            return clientBuilder(basePath -> realIssuerWithConnectionReuse(protocol, host, port, basePath));
        };
    }

    public <T> T issue(final HttpClientRequestBuilder<T> requestBuilder) {
        return issue(requestBuilder.build(basePath));
    }

    @SuppressWarnings("unchecked")
    public <T> T issue(final HttpClientRequest<T> request) {
        validateNotNull(request, "request");
        return issuer.issue(request, response -> {
            final Class<T> targetType = request.targetType();
            final ClientResponseMapper<T> responseMapper = (ClientResponseMapper<T>) responseMappers.get(targetType);
            return responseMapper.map(response, targetType);
        });
    }

    @Override
    public void close() {
        issuer.close();
    }
}
