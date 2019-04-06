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

import com.envimate.httpmate.chains.rules.*;
import com.envimate.messageMate.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.envimate.httpmate.chains.HttpMateChainKeys.EXCEPTION;
import static com.envimate.messageMate.channel.ChannelBuilder.aChannel;
import static com.envimate.messageMate.channel.action.Subscription.subscription;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Chain {
    @Getter(AccessLevel.PUBLIC)
    private final Action defaultAction;
    @Getter(AccessLevel.PUBLIC)
    private final Action exceptionAction;
    @Getter(AccessLevel.PUBLIC)
    private final List<Rule> rules;
    @Getter
    private final Channel<ProcessingContext> processChannel;
    private final String name;

    static Chain chain(final Action defaultAction,
                       final Action exceptionAction,
                       final String name) {
        final Channel<ProcessingContext> processChannel = aChannel(ProcessingContext.class)
                .withDefaultAction(subscription()).build();
        return new Chain(defaultAction, exceptionAction, new LinkedList<>(), processChannel, name);
    }

    public void addProcessor(final Processor processor) {
        this.processChannel.addProcessFilter((processingContext, filterActions) -> {
            processor.apply(processingContext.getPayload().getMetaData());
            filterActions.pass(processingContext);
        });
    }

    public void addRoutingRule(final Rule routingRule) {
        rules.add(routingRule);
    }

    void accept(final ProcessingContext processingContext) {
        final MetaData metaData = processingContext.getMetaData();
        try {
            processChannel.send(processingContext);
            final Action action = rules.stream()
                    .filter(rule -> rule.matches(metaData))
                    .findFirst()
                    .map(Rule::action)
                    .orElse(defaultAction);

            handleAction(action, processingContext);
        } catch (final Exception e) {
            metaData.set(EXCEPTION, e);
            handleAction(exceptionAction, processingContext);
        }
    }

    private static void handleAction(final Action action,
                                     final ProcessingContext processingContext) {
        if (action instanceof Jump) {
            final Jump jump = (Jump) action;
            final Chain targetChain = jump.target().orElseThrow();
            targetChain.accept(processingContext);
        } else if (action instanceof Consume) {
            final Consumer<MetaData> consumer = processingContext.getConsumer();
            final MetaData metaData = processingContext.getMetaData();
            consumer.accept(metaData);
        } else if (action instanceof Drop) {
            // do nothing
        } else {
            throw new RuntimeException("Unknown action: " + action.getClass().getName());
        }
    }

    public String getName() {
        return name;
    }
}
