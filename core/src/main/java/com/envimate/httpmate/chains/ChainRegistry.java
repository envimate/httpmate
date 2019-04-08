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

package com.envimate.httpmate.chains;

import com.envimate.httpmate.chains.rules.Action;
import com.envimate.httpmate.chains.rules.Processor;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.envimate.httpmate.chains.Chain.chain;
import static com.envimate.httpmate.chains.GraphCreator.createGraph;
import static com.envimate.httpmate.chains.ProcessingContext.processingContext;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class ChainRegistry {
    private final Map<ChainName, Chain> namedChains = new HashMap<>();

    public static ChainRegistry emptyChainRegistry() {
        return new ChainRegistry();
    }

    public Chain createChain(final ChainName name,
                             final Action defaultAction,
                             final Action exceptionAction) {
        final Chain chain = chain(defaultAction, exceptionAction, name);
        namedChains.put(name, chain);
        return chain;
    }

    public Chain getChainFor(final ChainName chainName) {
        validateNotNull(chainName, "chainName");
        if (namedChains.containsKey(chainName)) {
            return namedChains.get(chainName);
        } else {
            throw new NoChainForNameException(chainName);
        }
    }

    public void addProcessorToChain(final ChainName chainName,
                                    final Processor processor) {
        validateNotNull(chainName, "chainName");
        validateNotNull(processor, "processor");
        final Chain chain = getChainFor(chainName);
        chain.addProcessor(processor);
    }

    public void putIntoChain(final ChainName chainName,
                             final MetaData initialMetaData,
                             final Consumer<MetaData> consumer) {
        final Chain chain = getChainFor(chainName);
        final ProcessingContext processingContext = processingContext(this, initialMetaData, consumer);
        chain.accept(processingContext);
    }

    public String dump() {
        return createGraph(namedChains);
    }
}
