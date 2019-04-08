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

package com.envimate.httpmate;

import com.envimate.httpmate.builder.EventStage1;
import com.envimate.httpmate.builder.UseCaseStage1;
import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.chains.ChainRegistry;
import com.envimate.httpmate.chains.HttpMateChains;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.convenience.endpoints.PureJavaEndpoint;
import com.envimate.httpmate.mapper.ResponseHandler;
import com.envimate.messageMate.messageBus.MessageBus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static com.envimate.httpmate.EventDrivenBuilder.eventDrivenBuilder;
import static com.envimate.httpmate.UseCaseDrivenBuilder.useCaseDrivenBuilder;
import static com.envimate.httpmate.chains.HttpMateChainKeys.LOGGER;
import static com.envimate.httpmate.util.Validators.validateNotNull;

/**
 * A configured {@link HttpMate} instance. Can be deployed using an endpoint like
 * {@link PureJavaEndpoint}
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMate {
    private final ChainRegistry chainRegistry;
    private final MessageBus messageBus;
    private final Logger logger;

    static HttpMate httpMate(final ChainRegistry chainRegistry,
                             final MessageBus messageBus,
                             final Logger logger) {
        validateNotNull(chainRegistry, "chainRegistry");
        validateNotNull(messageBus, "messageBus");
        validateNotNull(logger, "logger");
        return new HttpMate(chainRegistry, messageBus, logger);
    }

    private void initMetaData(final MetaData metaData) {
        metaData.set(LOGGER, logger);
    }

    public void handle(final ChainName chainName,
                       final MetaData metaData) {
        initMetaData(metaData);
        chainRegistry.putIntoChain(chainName, metaData, metaData1 -> {
        });
    }

    public void handleRequest(final MetaData metaData,
                              final ResponseHandler responseHandler) {
        initMetaData(metaData);
        chainRegistry.putIntoChain(HttpMateChains.PRE_PROCESS, metaData, finalMetaData -> {
            try {
                responseHandler.handleResponse(metaData);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }

    public String dumpChains() {
        return chainRegistry.dump();
    }

    /**
     * Enters a fluent builder to configure a {@link HttpMate} instance.
     * This provides a wide range of configuration options.
     *
     * @return a fluent builder to configure a {@link HttpMate} instance
     */
    public static UseCaseStage1<UseCaseDrivenBuilder.Stage1> aHttpMateInstance() {
        return useCaseDrivenBuilder();
    }

    public static EventStage1<EventDrivenBuilder.Stage1> aHttpMateDispatchingEventsUsing(final MessageBus messageBus) {
        return eventDrivenBuilder(messageBus);
    }
}
