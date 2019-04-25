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

package com.envimate.httpmate.convenience.cors;

import com.envimate.httpmate.chains.ChainExtender;
import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.http.Headers;
import com.envimate.httpmate.http.HttpRequestMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.HttpMateChains.*;
import static com.envimate.httpmate.chains.ChainName.chainName;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.http.Http.StatusCodes.OK;
import static com.envimate.httpmate.http.HttpRequestMethod.OPTIONS;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.lang.String.join;
import static java.util.stream.Collectors.joining;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CorsModule implements ChainModule {
    private static final String ALLOW_ORIGIN_KEY = "Access-Control-Allow-Origin";
    private static final String REQUEST_METHOD_KEY = "Access-Control-Request-Method";
    private static final String ALLOW_METHOD_KEY = "Access-Control-Allow-Methods";
    private static final String REQUEST_HEADERS_KEY = "Access-Control-Request-Headers";
    private static final String ALLOW_HEADERS_KEY = "Access-Control-Allow-Headers";

    private static final ChainName CORS_CHAIN = chainName("CORS");

    private final String allowedOrigin;
    private final List<HttpRequestMethod> allowedMethods;
    private final List<String> allowedHeaders;

    public static ChainModule corsModule(final String allowedOrigin,
                                            final List<HttpRequestMethod> allowedMethods,
                                            final List<String> allowedHeaders) {
        validateNotNullNorEmpty(allowedOrigin, "allowedOrigin");
        validateNotNull(allowedMethods, "allowedMethods");
        validateNotNull(allowedHeaders, "allowedHeaders");
        return new CorsModule(allowedOrigin, allowedMethods, allowedHeaders);
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.createChain(CORS_CHAIN, jumpTo(POST_PROCESS), jumpTo(EXCEPTION_OCCURRED));
        extender.addProcessor(CORS_CHAIN, metaData -> {
            final Headers headers = metaData.get(HEADERS);
            final Map<String, String> responseHeaders = new HashMap<>();
            headers.getHeader(REQUEST_HEADERS_KEY)
                    .ifPresent(requestedHeaders -> responseHeaders.put(ALLOW_HEADERS_KEY, requestedHeaders));
            headers.getHeader(REQUEST_METHOD_KEY)
                    .ifPresent(requestedMethods -> responseHeaders.put(ALLOW_METHOD_KEY, requestedMethods));
            metaData.set(RESPONSE_HEADERS, responseHeaders);
            metaData.set(RESPONSE_STATUS, OK);
            metaData.set(STRING_RESPONSE, "OK");
        });

        extender.routeIfEquals(PRE_PROCESS, jumpTo(CORS_CHAIN), METHOD, OPTIONS);

        extender.addProcessor(PREPARE_RESPONSE, metaData -> {
            final Map<String, String> headers = metaData.get(RESPONSE_HEADERS);
            headers.put(ALLOW_ORIGIN_KEY, allowedOrigin);
            headers.put(REQUEST_METHOD_KEY, allowedMethods.stream()
                    .map(Enum::name)
                    .collect(joining(", ")));
            headers.put(ALLOW_HEADERS_KEY, join(", ", allowedHeaders));
        });
    }
}
