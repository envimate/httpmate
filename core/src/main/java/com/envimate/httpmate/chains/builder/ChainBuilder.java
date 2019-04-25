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

package com.envimate.httpmate.chains.builder;

import com.envimate.httpmate.chains.ChainExtender;
import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.chains.Processor;
import com.envimate.httpmate.chains.rules.Action;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.httpmate.chains.builder.ChainBuilderEntry.chainBuilderEntry;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChainBuilder {
    private final ChainExtender extender;
    private final List<ChainBuilderEntry> chains;
    private ChainName exceptionChainName;

    public static ChainBuilder extendAChainWith(final ChainExtender chainExtender) {
        return new ChainBuilder(chainExtender, new LinkedList<>());
    }

    public ChainBuilder append(final ChainName chainName, final Processor... processors) {
        return append(chainName, asList(processors));
    }

    public ChainBuilder append(final ChainName chainName, final List<? extends Processor> processors) {
        validateNotNull(chainName, "chainName");
        validateNotNull(processors, "processors");
        final ChainBuilderEntry entry = chainBuilderEntry(chainName, processors);
        chains.add(entry);
        return this;
    }

    public ChainBuilder withTheExceptionChain(final ChainName exceptionChainName) {
        validateNotNull(exceptionChainName, "exceptionChainName");
        this.exceptionChainName = exceptionChainName;
        return this;
    }

    public void withTheFinalAction(final Action finalAction) {
        reverse(chains);
        final Action exceptionAction = jumpTo(exceptionChainName);
        Action action = finalAction;
        for (final ChainBuilderEntry entry : chains) {
            final ChainName name = entry.chainName();
            createChain(extender, name, action, exceptionAction, entry.processors());
            action = jumpTo(name);
        }
    }

    private static void createChain(final ChainExtender chainExtender,
                                    final ChainName chainName,
                                    final Action action,
                                    final Action exceptionAction,
                                    final List<? extends Processor> processors) {
        chainExtender.createChain(chainName, action, exceptionAction);
        processors.forEach(processor -> chainExtender.addProcessor(chainName, processor));
    }
}
