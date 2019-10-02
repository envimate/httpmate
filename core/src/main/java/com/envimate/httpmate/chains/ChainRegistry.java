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

package com.envimate.httpmate.chains;

import com.envimate.httpmate.chains.rules.*;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.envimate.httpmate.chains.Chain.chain;
import static com.envimate.httpmate.chains.ChainExtender.chainExtender;
import static com.envimate.httpmate.chains.GraphCreator.createGraph;
import static com.envimate.httpmate.chains.MetaDataKey.metaDataKey;
import static com.envimate.httpmate.chains.ProcessingContext.processingContext;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class ChainRegistry {
    public static final MetaDataKey<ChainRegistry> CHAIN_REGISTRY = metaDataKey("CHAIN_REGISTRY");

    private final Map<ChainName, Chain> namedChains;
    private final MetaData metaData;

    static ChainRegistry emptyChainRegistry(final MetaData metaData) {
        validateNotNull(metaData, "metaData");
        final ChainRegistry chainRegistry = new ChainRegistry(new HashMap<>(), metaData);
        chainRegistry.metaData.set(CHAIN_REGISTRY, chainRegistry);
        return chainRegistry;
    }

    void extend(final ChainModule module) {
        validateNotNull(module, "module");
        final ModuleIdentifier identifier = module.identifier();
        final ChainExtender extender = chainExtender(this, identifier, metaData);
        module.register(extender);
    }

    public void putIntoChain(final ChainName chainName,
                             final MetaData initialMetaData,
                             final Consumer<MetaData> consumer) {
        final ProcessingContext processingContext = processingContext(initialMetaData, consumer);
        accept(chainName, processingContext);
    }

    private void accept(final ChainName chainName,
                        final ProcessingContext processingContext) {
        final Chain chain = getChainFor(chainName);
        final Action action = chain.accept(processingContext);
        handleAction(action, processingContext);
    }

    private void handleAction(final Action action,
                              final ProcessingContext processingContext) {
        if (action instanceof Jump) {
            final Jump jump = (Jump) action;
            final ChainName name = jump.target().orElseThrow();
            accept(name, processingContext);
        } else if (action instanceof Consume) {
            processingContext.consume();
        } else if (action instanceof Drop) {
            // do nothing
        } else {
            throw new RuntimeException("Unknown action: " + action.getClass().getName());
        }
    }

    public String dump() {
        return createGraph(namedChains, false).plot();
    }

    public <T> T getMetaDatum(final MetaDataKey<T> key) {
        validateNotNull(key, "key");
        return metaData.get(key);
    }

    public <T> Optional<T> getOptionalMetaDatum(final MetaDataKey<T> key) {
        validateNotNull(key, "key");
        return metaData.getOptional(key);
    }

    public <T> void addMetaDatum(final MetaDataKey<T> key, final T value) {
        validateNotNull(key, "key");
        validateNotNull(value, "value");
        metaData.set(key, value);
    }

    void createChain(final ChainName name,
                     final Action defaultAction,
                     final Action exceptionAction,
                     final ModuleIdentifier moduleIdentifier) {
        validateNotNull(name, "name");
        validateNotNull(defaultAction, "defaultAction");
        validateNotNull(exceptionAction, "exceptionAction");
        validateNotNull(moduleIdentifier, "moduleIdentifier");
        if(namedChains.containsKey(name)) {
            throw new RuntimeException(format("A chain with name '%s' already exists", name.name()));
        }
        final Chain chain = chain(defaultAction, exceptionAction, name, moduleIdentifier);
        namedChains.put(name, chain);
    }

    void prependProcessorToChain(final ChainName chainName,
                                 final RegisteredProcessor processor) {
        validateNotNull(chainName, "chainName");
        validateNotNull(processor, "processor");
        final Chain chain = getChainFor(chainName);
        chain.prependProcessor(processor);
    }

    void appendProcessorToChain(final ChainName chainName,
                                final RegisteredProcessor processor) {
        validateNotNull(chainName, "chainName");
        validateNotNull(processor, "processor");
        final Chain chain = getChainFor(chainName);
        chain.appendProcessor(processor);
    }

    void addRoutingRouleToChain(final ChainName chainName,
                                final Rule rule) {
        validateNotNull(chainName, "chainName");
        validateNotNull(rule, "rule");
        final Chain chain = getChainFor(chainName);
        chain.addRoutingRule(rule);
    }

    private Chain getChainFor(final ChainName chainName) {
        validateNotNull(chainName, "chainName");
        if (namedChains.containsKey(chainName)) {
            return namedChains.get(chainName);
        } else {
            throw new NoChainForNameException(chainName);
        }
    }
}
