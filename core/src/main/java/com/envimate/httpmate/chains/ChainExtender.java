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

import com.envimate.httpmate.chains.rules.Action;
import com.envimate.httpmate.chains.rules.Rule;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;
import java.util.function.Predicate;

import static com.envimate.httpmate.chains.RegisteredProcessor.registeredProcessor;
import static com.envimate.httpmate.chains.rules.Rule.rule;
import static com.envimate.httpmate.chains.rules.RuleDescription.ruleDescription;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChainExtender {
    private final ChainRegistry chainRegistry;
    private final ModuleIdentifier moduleIdentifier;
    private final MetaData metaData;

    static ChainExtender chainExtender(final ChainRegistry chainRegistry,
                                       final ModuleIdentifier moduleIdentifier,
                                       final MetaData metaData) {
        validateNotNull(chainRegistry, "chainRegistry");
        validateNotNull(moduleIdentifier, "moduleIdentifier");
        return new ChainExtender(chainRegistry, moduleIdentifier, metaData);
    }

    public void createChain(final ChainName name,
                            final Action defaultAction,
                            final Action exceptionAction) {
        chainRegistry.createChain(name, defaultAction, exceptionAction, moduleIdentifier);
    }

    public void addProcessor(final ChainName chainName,
                             final Processor processor) {
        final RegisteredProcessor registeredProcessor = registeredProcessor(moduleIdentifier, processor);
        chainRegistry.addProcessorToChain(chainName, registeredProcessor);
    }

    public <T> void routeIfEquals(final ChainName name,
                                  final Action action,
                                  final MetaDataKey<T> key,
                                  final T value) {
        routeIf(name, action, key, value::equals, format("%s = %s", key.key(), value.toString()));
    }

    public void routeIfFlagIsSet(final ChainName name,
                                 final Action action,
                                 final MetaDataKey<Boolean> flag) {
        routeIf(name, action, flag, bool -> bool, flag.key());
    }

    public <T> void routeIfSet(final ChainName name,
                               final Action action,
                               final MetaDataKey<?> key) {
        routeIf(name, action, key, x -> true, key.key() + " present");
    }

    public <T> void routeIf(final ChainName name,
                            final Action action,
                            final MetaDataKey<T> key,
                            final Predicate<T> predicate,
                            final String description) {
        route(name, action, m -> m.getOptional(key)
                .map(predicate::test)
                .orElse(false), description);
    }

    public void route(final ChainName name,
                      final Action action,
                      final Predicate<MetaData> matcher,
                      final String description) {
        final Rule rule = rule(moduleIdentifier, matcher, action, ruleDescription(description));
        chainRegistry.addRoutingRouleToChain(name, rule);
    }

    public <T> T getMetaDatum(final MetaDataKey<T> key) {
        return metaData.get(key);
    }

    public <T> Optional<T> getOptionalMetaDatum(final MetaDataKey<T> key) {
        return metaData.getOptional(key);
    }
}
