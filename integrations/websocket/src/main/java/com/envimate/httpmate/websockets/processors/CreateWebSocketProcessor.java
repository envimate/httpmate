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

package com.envimate.httpmate.websockets.processors;

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.chains.rules.Processor;
import com.envimate.httpmate.websockets.*;
import com.envimate.httpmate.websockets.registry.WebSocketId;
import com.envimate.httpmate.websockets.registry.WebSocketRegistry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.websockets.WEBSOCKET_CHAIN_KEYS.*;
import static com.envimate.httpmate.websockets.WebSocket.webSocket;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateWebSocketProcessor implements Processor {
    private final WebSocketRegistry webSocketRegistry;

    public static Processor createWebSocketProcessor(final WebSocketRegistry webSocketRegistry) {
        validateNotNull(webSocketRegistry, "webSocketRegistry");
        return new CreateWebSocketProcessor(webSocketRegistry);
    }

    @Override
    public void apply(final MetaData metaData) {
        metaData.getOptional(WEBSOCKET_MAPPING).ifPresent(webSocketMapping -> createWebSocket(webSocketMapping, metaData));
    }

    private void createWebSocket(final WebSocketMapping webSocketMapping,
                                 final MetaData metaData) {
        final MetaDataEntriesToSave metaDataEntriesToSave = webSocketMapping.metaDataEntriesToSave();
        final SavedMetaDataEntries savedMetaDataEntries = metaDataEntriesToSave.save(metaData);
        final WebSocketId id = metaData.get(WEBSOCKET_ID);
        final WebSocketDelegate webSocketDelegate = metaData.get(WEBSOCKET_DELEGATE);
        final WebSocket webSocket = webSocket(id,
                webSocketDelegate,
                savedMetaDataEntries);
        webSocketRegistry.register(id, webSocket);
        metaData.set(WEBSOCKET_ACCEPTED, true);
    }
}
