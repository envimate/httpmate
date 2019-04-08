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

package com.envimate.httpmate.chains.builder;

import com.envimate.httpmate.chains.Chain;
import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.chains.ChainRegistry;
import com.envimate.httpmate.chains.rules.Action;
import com.envimate.httpmate.chains.rules.Processor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.httpmate.chains.ChainRegistry.emptyChainRegistry;
import static com.envimate.httpmate.chains.builder.ChainBuilderEntry.chainBuilderEntry;
import static com.envimate.httpmate.chains.rules.Consume.consume;
import static com.envimate.httpmate.chains.rules.Drop.drop;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChainBuilder {
    private final List<ChainBuilderEntry> chains;

    public static ChainBuilder startingAChainWith(final ChainName chainName, final Processor... processors) {
        final ChainBuilder chainBuilder = new ChainBuilder(new LinkedList<>());
        chainBuilder.append(chainName, processors);
        return chainBuilder;
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

    public ChainRegistry withTheExceptionChain(final ChainName exceptionChainName,
                                               final Processor... exceptionProcessors) {
        validateNotNull(exceptionChainName, "exceptionChainName");
        validateNotNull(exceptionProcessors, "exceptionProcessors");
        final ChainRegistry chainRegistry = emptyChainRegistry();
        final Chain exceptionChain = createChain(
                chainRegistry, exceptionChainName, consume(), drop(), asList(exceptionProcessors));

        reverse(chains);
        Action action = consume();
        final Action exceptionAction = jumpTo(exceptionChain);
        for (final ChainBuilderEntry entry : chains) {
            final Chain chain = createChain(
                    chainRegistry, entry.chainName(), action, exceptionAction, entry.processors());
            action = jumpTo(chain);
        }
        return chainRegistry;
    }

    private static Chain createChain(final ChainRegistry chainRegistry,
                                     final ChainName chainName,
                                     final Action action,
                                     final Action exceptionAction,
                                     final List<? extends Processor> processors) {
        final Chain chain = chainRegistry.createChain(chainName, action, exceptionAction);
        processors.forEach(chain::addProcessor);
        return chain;
    }
}
