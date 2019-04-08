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

package com.envimate.httpmate.convenience.debug;

import com.envimate.httpmate.Module;
import com.envimate.httpmate.chains.Chain;
import com.envimate.httpmate.chains.ChainRegistry;
import com.envimate.httpmate.chains.HttpMateChains;
import com.envimate.httpmate.chains.rules.Rule;
import com.envimate.httpmate.path.PathTemplate;
import com.envimate.messageMate.messageBus.MessageBus;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.chains.ChainName.chainName;
import static com.envimate.httpmate.chains.HttpMateChainKeys.PATH;
import static com.envimate.httpmate.chains.HttpMateChainKeys.STRING_RESPONSE;
import static com.envimate.httpmate.chains.HttpMateChains.POST_SERIALIZATION;
import static com.envimate.httpmate.chains.rules.Drop.drop;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebugModule implements Module {
    private final PathTemplate pathTemplate = PathTemplate.pathTemplate("/internals");

    public static Module debugModule() {
        return new DebugModule();
    }

    @Override
    public void register(final ChainRegistry chainRegistry,
                         final MessageBus messageBus) {
        final Chain nextChain = chainRegistry.getChainFor(POST_SERIALIZATION);
        final Chain debugChain = chainRegistry.createChain(chainName("DEBUG"), jumpTo(nextChain), drop());
        debugChain.addProcessor(metaData -> {
            final String dump = chainRegistry.dump();
            metaData.set(STRING_RESPONSE, dump);
        });

        final Chain attachChain = chainRegistry.getChainFor(HttpMateChains.PRE_PROCESS);
        attachChain.addRoutingRule(Rule.jumpRule(debugChain, metaData -> {
            final String path = metaData.get(PATH);
            return pathTemplate.matches(path);
        }));
    }
}
