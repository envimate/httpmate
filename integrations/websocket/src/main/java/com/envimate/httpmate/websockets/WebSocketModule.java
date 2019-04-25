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

package com.envimate.httpmate.websockets;

import com.envimate.httpmate.MetricsProvider;
import com.envimate.httpmate.chains.ChainExtender;
import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.chains.DependencyRegistry;
import com.envimate.httpmate.websockets.registry.WebSocketRegistry;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.httpmate.HttpMateChains.*;
import static com.envimate.httpmate.chains.rules.Consume.consume;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.websockets.WebSocketMetrics.NUMBER_OF_ACTIVE_WEB_SOCKETS;
import static com.envimate.httpmate.websockets.WebsocketChainKeys.*;
import static com.envimate.httpmate.websockets.WebsocketChains.*;
import static com.envimate.httpmate.websockets.processors.ActivateWebSocketProcessor.activateWebSocketProcessor;
import static com.envimate.httpmate.websockets.processors.CloseWebSocketProcessor.closeWebSocketProcessor;
import static com.envimate.httpmate.websockets.processors.CreateWebSocketProcessor.createWebSocketProcessor;
import static com.envimate.httpmate.websockets.processors.DetermineWebSocketTypeProcessor.determineWebSocketTypeProcessor;
import static com.envimate.httpmate.websockets.processors.HandleNewWebSocketMessageProcessor.handleNewWebSocketMessageProcessor;
import static com.envimate.httpmate.websockets.processors.RemoveWebSocketFromRegistryProcessor.removeWebSocketFromRegistryProcessor;
import static com.envimate.httpmate.websockets.processors.SendToWebSocketsProcessor.sendToWebSocketsProcessor;
import static com.envimate.httpmate.websockets.processors.WebSocketInitializationProcessor.webSocketInitializationProcessor;
import static com.envimate.httpmate.websockets.registry.WebSocketRegistry.webSocketRegistry;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketModule implements ChainModule {
    private final List<WebSocketMapping> webSocketMappings = new LinkedList<>();

    public static WebSocketModule webSocketModule() {
        return new WebSocketModule();
    }

    public void addWebSocketMapping(final WebSocketMapping webSocketMapping) {
        validateNotNull(webSocketMapping, "webSocketMapping");
        webSocketMappings.add(webSocketMapping);
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final MetricsProvider<Integer> metricsProvider =
                dependencyRegistry.createMetricsProvider(NUMBER_OF_ACTIVE_WEB_SOCKETS, 0);
        dependencyRegistry.setMetaDatum(WEBSOCKET_REGISTRY, webSocketRegistry(metricsProvider));
    }

    @Override
    public void register(final ChainExtender extender) {
        final WebSocketRegistry registry = extender.getMetaDatum(WEBSOCKET_REGISTRY);
        createSkeleton(extender);
        extender.addProcessor(INIT, webSocketInitializationProcessor(registry));
        extender.addProcessor(WEBSOCKET_ESTABLISHMENT, createWebSocketProcessor(registry));
        extender.addProcessor(DETERMINE_WEBSOCKET_TYPE, determineWebSocketTypeProcessor(webSocketMappings));
        extender.addProcessor(WEBSOCKET_OPEN, activateWebSocketProcessor(registry));
        extender.addProcessor(SEND_TO_WEBSOCKETS, sendToWebSocketsProcessor());
        extender.addProcessor(WEBSOCKET_CLOSED, removeWebSocketFromRegistryProcessor(registry));
        extender.addProcessor(WEBSOCKET_CLOSE, closeWebSocketProcessor(registry));
        extender.addProcessor(WEBSOCKET_MESSAGE, handleNewWebSocketMessageProcessor(registry));
    }

    private static void createSkeleton(final ChainExtender extender) {
        extender.createChain(WEBSOCKET_ESTABLISHMENT, consume(), jumpTo(EXCEPTION_OCCURRED));
        extender.createChain(DETERMINE_WEBSOCKET_TYPE, jumpTo(WEBSOCKET_ESTABLISHMENT), jumpTo(EXCEPTION_OCCURRED));
        extender.routeIfSet(PROCESS_HEADERS, jumpTo(DETERMINE_WEBSOCKET_TYPE), WEBSOCKET_ID);
        extender.createChain(WEBSOCKET_OPEN, consume(), jumpTo(EXCEPTION_OCCURRED));
        extender.createChain(WEBSOCKET_MESSAGE, jumpTo(PROCESS_BODY), jumpTo(EXCEPTION_OCCURRED));
        extender.createChain(SEND_TO_WEBSOCKETS, consume(), jumpTo(EXCEPTION_OCCURRED));
        extender.createChain(WEBSOCKET_CLOSED, consume(), jumpTo(EXCEPTION_OCCURRED));
        extender.createChain(WEBSOCKET_CLOSE, consume(), jumpTo(EXCEPTION_OCCURRED));

        extender.routeIfFlagIsSet(INIT, jumpTo(WEBSOCKET_MESSAGE), IS_WEBSOCKET_MESSAGE);
        extender.routeIfFlagIsSet(POST_PROCESS, jumpTo(SEND_TO_WEBSOCKETS), IS_WEBSOCKET_MESSAGE);
    }
}
