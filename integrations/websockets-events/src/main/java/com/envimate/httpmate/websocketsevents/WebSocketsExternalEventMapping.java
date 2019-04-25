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

package com.envimate.httpmate.websocketsevents;

import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.events.ExternalEventMapping;
import com.envimate.httpmate.websockets.WebSocket;
import com.envimate.httpmate.websockets.WebSocketForEventFilter;
import com.envimate.httpmate.websockets.registry.WebSocketRegistry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.envimate.httpmate.chains.MetaData.emptyMetaData;
import static com.envimate.httpmate.events.EventModule.EVENT_RETURN_VALUE;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.websockets.WebsocketChainKeys.WEBSOCKET_REGISTRY;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketsExternalEventMapping implements ExternalEventMapping {
    private final ChainName jumpTarget;
    private final WebSocketForEventFilter filter;
    private final BiConsumer<List<WebSocket>, MetaData> consumer;

    static ExternalEventMapping webSocketsExternalEventMapping(final ChainName jumpTarget,
                                                               final WebSocketForEventFilter filter,
                                                               final BiConsumer<List<WebSocket>, MetaData> consumer) {
        validateNotNull(jumpTarget, "jumpTarget");
        validateNotNull(filter, "filter");
        validateNotNull(consumer, "consumer");
        return new WebSocketsExternalEventMapping(jumpTarget, filter, consumer);
    }

    @Override
    public Optional<ChainName> jumpTarget() {
        return of(jumpTarget);
    }

    @Override
    public void apply(final MetaData metaData) {
        final Map<String, Object> event = metaData.get(EVENT_RETURN_VALUE).orElseThrow();
        final WebSocketRegistry registry = metaData.get(WEBSOCKET_REGISTRY);
        final List<WebSocket> webSockets = registry.allActiveWebSockets().stream()
                .filter(webSocket -> {
                    final MetaData temporaryMetaData = emptyMetaData();
                    webSocket.savedMetaDataEntries().restoreTo(temporaryMetaData);
                    return filter.test(temporaryMetaData, event);
                })
                .collect(toList());
        consumer.accept(webSockets, metaData);
    }
}
