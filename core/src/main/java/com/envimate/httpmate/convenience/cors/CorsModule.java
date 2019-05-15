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
import com.envimate.httpmate.convenience.cors.policy.ResourceSharingPolicy;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.HttpMateChainKeys.METHOD;
import static com.envimate.httpmate.HttpMateChains.*;
import static com.envimate.httpmate.chains.ChainName.chainName;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.convenience.cors.PreflightRequestProcessor.preflightRequestProcessor;
import static com.envimate.httpmate.convenience.cors.SimpleCrossOriginRequestProcessor.simpleCrossOriginRequestProcessor;
import static com.envimate.httpmate.http.HttpRequestMethod.OPTIONS;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CorsModule implements ChainModule {
    private static final ChainName CORS_CHAIN = chainName("CORS");
    private final ResourceSharingPolicy resourceSharingPolicy;

    public static ChainModule corsModule(final ResourceSharingPolicy resourceSharingPolicy) {
        validateNotNull(resourceSharingPolicy, "resourceSharingPolicy");
        return new CorsModule(resourceSharingPolicy);
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.createChain(CORS_CHAIN, jumpTo(POST_PROCESS), jumpTo(EXCEPTION_OCCURRED));

        extender.addProcessor(CORS_CHAIN, preflightRequestProcessor(resourceSharingPolicy));

        /*
        extender.addProcessor(CORS_CHAIN, metaData -> {
            final Headers headers = metaData.get(HEADERS);
            final Map<String, String> responseHeaders = new HashMap<>();
            headers.getHeader(ACCESS_CONTROL_REQUEST_HEADERS)
                    .ifPresent(requestedHeaders -> responseHeaders.put(ACCESS_CONTROL_ALLOW_HEADERS, requestedHeaders));
            headers.getHeader(ACCESS_CONTROL_REQUEST_METHOD)
                    .ifPresent(requestedMethods -> responseHeaders.put(ACCESS_CONTROL_ALLOW_METHODS, requestedMethods));
            metaData.set(RESPONSE_HEADERS, responseHeaders);
            metaData.set(RESPONSE_STATUS, OK);
            metaData.set(RESPONSE_STRING, "OK");
        });
         */

        extender.routeIfEquals(PRE_PROCESS, jumpTo(CORS_CHAIN), METHOD, OPTIONS);

        extender.addProcessor(PREPARE_RESPONSE, simpleCrossOriginRequestProcessor(resourceSharingPolicy));

        /*
        extender.addProcessor(PREPARE_RESPONSE, metaData -> {
            final Map<String, String> headers = metaData.get(RESPONSE_HEADERS);
            headers.put(ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);
            headers.put(ACCESS_CONTROL_REQUEST_METHOD, allowedMethods.stream()
                    .map(Enum::name)
                    .collect(joining(", ")));
            headers.put(ACCESS_CONTROL_ALLOW_HEADERS, join(", ", allowedHeaders));
        });
         */
    }
}
