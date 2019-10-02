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

package com.envimate.httpmate.convenience.debug;

import com.envimate.httpmate.chains.ChainExtender;
import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.chains.ChainRegistry;
import com.envimate.httpmate.path.PathTemplate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.HttpMateChainKeys.PATH;
import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_BODY_STRING;
import java.util.HashMap;

import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.HttpMateChains.POST_PROCESS;
import static com.envimate.httpmate.HttpMateChains.PRE_PROCESS;
import static com.envimate.httpmate.chains.ChainName.chainName;
import static com.envimate.httpmate.chains.ChainRegistry.CHAIN_REGISTRY;
import static com.envimate.httpmate.chains.rules.Drop.drop;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.http.Http.StatusCodes.OK;
import static com.envimate.httpmate.path.PathTemplate.pathTemplate;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebugModule implements ChainModule {
    private static final ChainName DEBUG_CHAIN = chainName("DEBUG");
    private static final PathTemplate PATH_TEMPLATE = pathTemplate("/internals");

    public static ChainModule debugModule() {
        return new DebugModule();
    }

    @Override
    public void register(final ChainExtender extender) {
        final ChainRegistry registry = extender.getMetaDatum(CHAIN_REGISTRY);
        extender.createChain(DEBUG_CHAIN, jumpTo(POST_PROCESS), drop());
        extender.appendProcessor(DEBUG_CHAIN, metaData -> {
            final String dump = registry.dump();
            metaData.set(RESPONSE_BODY_STRING, dump);
            metaData.set(RESPONSE_STATUS, OK);
            metaData.set(RESPONSE_HEADERS, new HashMap<>());
        });
        extender.routeIf(PRE_PROCESS, jumpTo(DEBUG_CHAIN), PATH, PATH_TEMPLATE::matches, PATH_TEMPLATE.toString());
    }
}
