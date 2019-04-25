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
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.httpmate.HttpMateChainKeys.EXCEPTION;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Chain {
    private final Action defaultAction;
    private final Action exceptionAction;
    private final List<Rule> rules;
    private final List<RegisteredProcessor> processors;
    private final ChainName name;
    private final ModuleIdentifier moduleIdentifier;

    static Chain chain(final Action defaultAction,
                       final Action exceptionAction,
                       final ChainName name,
                       final ModuleIdentifier moduleIdentifier) {
        validateNotNull(defaultAction, "defaultAction");
        validateNotNull(exceptionAction, "exceptionAction");
        validateNotNull(name, "name");
        validateNotNull(moduleIdentifier, "moduleIdentifier");
        return new Chain(defaultAction, exceptionAction, new LinkedList<>(), new LinkedList<>(), name, moduleIdentifier);
    }

    void addProcessor(final RegisteredProcessor processor) {
        validateNotNull(processor, "processor");
        processors.add(processor);
    }

    void addRoutingRule(final Rule routingRule) {
        rules.add(routingRule);
    }

    Action accept(final ProcessingContext processingContext) {
        final MetaData metaData = processingContext.metaData();
        try {
            processors.stream()
                    .map(RegisteredProcessor::processor)
                    .forEach(processor -> processor.apply(metaData));
            return rules.stream()
                    .filter(rule -> rule.matches(metaData))
                    .findFirst()
                    .map(Rule::action)
                    .orElse(defaultAction);
        } catch (final Exception e) {
            metaData.set(EXCEPTION, e);
            return exceptionAction;
        }
    }

    Action defaultAction() {
        return defaultAction;
    }

    Action exceptionAction() {
        return exceptionAction;
    }

    List<RegisteredProcessor> processors() {
        return processors;
    }

    List<Rule> rules() {
        return rules;
    }

    ModuleIdentifier getModuleIdentifier() {
        return moduleIdentifier;
    }

    public ChainName getName() {
        return name;
    }
}
