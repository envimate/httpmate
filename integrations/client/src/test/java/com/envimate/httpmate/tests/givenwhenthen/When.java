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

package com.envimate.httpmate.tests.givenwhenthen;

import com.envimate.httpmate.client.HttpClientRequestBuilder;
import com.envimate.httpmate.client.HttpMateClient;
import com.envimate.httpmate.tests.givenwhenthen.domain.ACustomPrimitive;
import com.envimate.mapmate.builder.MapMate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Consumer;

import static com.envimate.httpmate.client.HttpClientRequest.aGetRequestToThePath;
import static com.envimate.httpmate.client.HttpMateClient.aHttpMateClientForTheHost;
import static com.envimate.httpmate.tests.givenwhenthen.Then.then;
import static com.envimate.httpmate.util.Streams.inputStreamToString;
import static com.envimate.mapmate.builder.MapMate.aMapMate;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class When {
    private final int port;
    private final RequestLog requestLog;

    static When when(final int port,
                     final RequestLog requestLog) {
        return new When(port, requestLog);
    }

    public Then aRequestIsMadeToThePath(final String path) {
        return aRequestIsMade(httpMateClient -> httpMateClient.issue(aGetRequestToThePath(path)));
    }

    public Then aRequestIsMade(final HttpClientRequestBuilder<?> requestBuilder) {
        return aRequestIsMade(httpMateClient -> httpMateClient.issue(requestBuilder));
    }

    public Then aRequestIsMade(final Consumer<HttpMateClient> clientConsumer) {
        final MapMate mapMate = aMapMate(ACustomPrimitive.class.getPackageName()).build();
        final HttpMateClient client = aHttpMateClientForTheHost("localhost")
                .withThePort(port)
                .viaHttp()
                .withDefaultResponseMapping((response, targetType) -> {
                    final String stringContent = inputStreamToString(response.content());
                    return mapMate.deserializeJson(stringContent, targetType);
                })
                .build();
        clientConsumer.accept(client);
        return then(requestLog);
    }
}
