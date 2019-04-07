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

package com.envimate.httpmate.convenience.cors;

import com.envimate.httpmate.Module;
import com.envimate.httpmate.chains.Chain;
import com.envimate.httpmate.chains.ChainRegistry;
import com.envimate.httpmate.request.Headers;
import com.envimate.httpmate.request.HttpRequestMethod;
import com.envimate.messageMate.messageBus.MessageBus;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.envimate.httpmate.chains.HttpMateChainKeys.*;
import static com.envimate.httpmate.chains.HttpMateChains.*;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.chains.rules.Rule.jumpRule;
import static com.envimate.httpmate.convenience.Http.StatusCodes.OK;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.lang.String.join;
import static java.util.stream.Collectors.joining;

// "Access-Control-Allow-Credentials", "true"
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CorsModule implements Module {
    private static final String ALLOW_ORIGIN_KEY = "Access-Control-Allow-Origin";
    private static final String REQUEST_METHOD_KEY = "Access-Control-Request-Method";
    private static final String ALLOW_METHOD_KEY = "Access-Control-Allow-Methods";
    private static final String REQUEST_HEADERS_KEY = "Access-Control-Request-Headers";
    private static final String ALLOW_HEADERS_KEY = "Access-Control-Allow-Headers";

    private final String allowedOrigin;
    private final List<HttpRequestMethod> allowedMethods;
    private final List<String> allowedHeaders;

    public static Module corsModule(final String allowedOrigin,
                                    final List<HttpRequestMethod> allowedMethods,
                                    final List<String> allowedHeaders) {
        validateNotNullNorEmpty(allowedOrigin, "allowedOrigin");
        validateNotNull(allowedMethods, "allowedMethods");
        validateNotNull(allowedHeaders, "allowedHeaders");
        return new CorsModule(allowedOrigin, allowedMethods, allowedHeaders);
    }

    @Override
    public void register(final ChainRegistry chainRegistry,
                         final MessageBus messageBus) {
        final Chain exceptionChain = chainRegistry.getChainFor(EXCEPTION_OCCURRED);
        final Chain entryChain = chainRegistry.getChainFor(POST_SERIALIZATION);
        final Chain corsChain = chainRegistry.createChain("CORS", jumpTo(entryChain), jumpTo(exceptionChain));

        corsChain.addProcessor(metaData -> {
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

        final Chain attachChain = chainRegistry.getChainFor(PRE_PROCESS);
        attachChain.addRoutingRule(jumpRule(corsChain, metaData -> {
            final HttpRequestMethod httpRequestMethod = metaData.get(METHOD);
            return httpRequestMethod.equals(HttpRequestMethod.OPTIONS);
        }));

        chainRegistry.addProcessorToChain(POST_SERIALIZATION, metaData -> {
            final Map<String, String> headers = metaData.get(RESPONSE_HEADERS);
            headers.put(ALLOW_ORIGIN_KEY, allowedOrigin);
            headers.put(REQUEST_METHOD_KEY, allowedMethods.stream()
                    .map(Enum::name)
                    .collect(joining(", ")));
            headers.put(ALLOW_HEADERS_KEY, join(", ", allowedHeaders));
        });
    }
}
