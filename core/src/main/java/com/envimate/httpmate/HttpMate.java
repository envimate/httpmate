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

package com.envimate.httpmate;

import com.envimate.httpmate.chains.*;
import com.envimate.httpmate.closing.ClosingActions;
import com.envimate.httpmate.convenience.endpoints.PureJavaEndpoint;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Optional;

import static com.envimate.httpmate.LowLevelBuilder.LOW_LEVEL;
import static com.envimate.httpmate.closing.ClosingActions.CLOSING_ACTIONS;
import static com.envimate.httpmate.closing.ClosingActions.closingActions;
import static com.envimate.httpmate.util.Validators.validateNotNull;

/**
 * A configured {@link HttpMate} instance. Can be deployed using an endpoint like
 * {@link PureJavaEndpoint}
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMate implements AutoCloseable {
    private final ChainRegistry chainRegistry;

    public static HttpMate httpMate(final ChainRegistry chainRegistry) {
        validateNotNull(chainRegistry, "chainRegistry");
        return new HttpMate(chainRegistry);
    }

    public void handle(final ChainName chainName,
                       final MetaData metaData) {
        chainRegistry.putIntoChain(chainName, metaData, metaData1 -> {
        });
    }

    public void handleRequest(final MetaData metaData,
                              final FinalConsumer responseHandler) {
        chainRegistry.putIntoChain(HttpMateChains.INIT, metaData, finalMetaData -> {
            try {
                responseHandler.consume(metaData);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public <T> T getMetaDatum(final MetaDataKey<T> key) {
        validateNotNull(key, "key");
        return chainRegistry.getMetaDatum(key);
    }

    public <T> Optional<T> getOptionalMetaDatum(final MetaDataKey<T> key) {
        validateNotNull(key, "key");
        return chainRegistry.getOptionalMetaDatum(key);
    }

    public String dumpChains() {
        return chainRegistry.dump();
    }

    public static LowLevelBuilder aLowLevelHttpMate() {
        return anHttpMateConfiguredAs(LOW_LEVEL);
    }

    public static <T> T anHttpMateConfiguredAs(final HttpMateConfigurationType<T> type) {
        validateNotNull(type, "type");
        return type.configure();
    }

    @Override
    public void close() {
        final ClosingActions closingActions = chainRegistry.getMetaDatum(CLOSING_ACTIONS);
        closingActions.closeAll();
    }
}
