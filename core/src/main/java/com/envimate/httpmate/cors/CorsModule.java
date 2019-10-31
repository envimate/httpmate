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

package com.envimate.httpmate.cors;

import com.envimate.httpmate.chains.ChainExtender;
import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.cors.policy.ResourceSharingPolicy;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.HttpMateChainKeys.METHOD;
import static com.envimate.httpmate.HttpMateChains.*;
import static com.envimate.httpmate.chains.ChainName.chainName;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.cors.PreflightRequestProcessor.preflightRequestProcessor;
import static com.envimate.httpmate.cors.SimpleCrossOriginRequestProcessor.simpleCrossOriginRequestProcessor;
import static com.envimate.httpmate.http.HttpRequestMethod.OPTIONS;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CorsModule implements ChainModule {
    private static final ChainName CORS_CHAIN = chainName("CORS");
    private volatile ResourceSharingPolicy resourceSharingPolicy;

    public static ChainModule corsModule() {
        return new CorsModule();
    }

    public void setResourceSharingPolicy(final ResourceSharingPolicy resourceSharingPolicy) {
        validateNotNull(resourceSharingPolicy, "resourceSharingPolicy");
        this.resourceSharingPolicy = resourceSharingPolicy;
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.createChain(CORS_CHAIN, jumpTo(POST_PROCESS), jumpTo(EXCEPTION_OCCURRED));
        extender.appendProcessor(CORS_CHAIN, preflightRequestProcessor(resourceSharingPolicy));
        extender.routeIfEquals(PRE_PROCESS, jumpTo(CORS_CHAIN), METHOD, OPTIONS);
        extender.appendProcessor(PREPARE_RESPONSE, simpleCrossOriginRequestProcessor(resourceSharingPolicy));
    }
}
