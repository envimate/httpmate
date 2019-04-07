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

package com.envimate.httpmate.websockets;

import com.envimate.httpmate.Module;
import com.envimate.httpmate.chains.Chain;
import com.envimate.httpmate.chains.ChainRegistry;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.websockets.registry.WebSocketRegistry;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.envimate.httpmate.chains.HttpMateChainKeys.EVENT_RETURN_VALUE;
import static com.envimate.httpmate.chains.HttpMateChains.*;
import static com.envimate.httpmate.chains.MetaData.emptyMetaData;
import static com.envimate.httpmate.chains.rules.Consume.consume;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.chains.rules.Rule.jumpRule;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.websockets.WebsocketChains.*;
import static com.envimate.httpmate.websockets.WebsocketChainKeys.IS_WEBSOCKET;
import static com.envimate.httpmate.websockets.WebsocketChainKeys.WEBSOCKET_ID;
import static com.envimate.httpmate.websockets.WebSocketModuleBuilder.webSocketModuleBuilder;
import static com.envimate.httpmate.websockets.processors.ActivateWebSocketProcessor.activateWebSocketProcessor;
import static com.envimate.httpmate.websockets.processors.CloseWebSocketProcessor.closeWebSocketProcessor;
import static com.envimate.httpmate.websockets.processors.CreateWebSocketProcessor.createWebSocketProcessor;
import static com.envimate.httpmate.websockets.processors.DetermineEventForWebSockets.determineEventForWebSockets;
import static com.envimate.httpmate.websockets.processors.DetermineWebSocketTypeProcessor.determineWebSocketTypeProcessor;
import static com.envimate.httpmate.websockets.processors.HandleNewWebSocketMessageProcessor.handleNewWebSocketMessageProcessor;
import static com.envimate.httpmate.websockets.processors.RemoveWebSocketFromRegistryProcessor.removeWebSocketFromRegistryProcessor;
import static com.envimate.httpmate.websockets.processors.SendToWebSocketsProcessor.sendToWebSocketsProcessor;
import static com.envimate.httpmate.websockets.registry.WebSocketRegistry.webSocketRegistry;
import static java.util.Optional.of;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketModule implements Module {
    private final List<WebSocketMapping> webSocketMappings;
    private final List<EventMapping> outgoingEventMappings;
    private final List<IncomingEventMapping> incomingEventMappings;
    private final List<IncomingEventMapping> closeEventMappings;
    private final WebSocketRegistry registry;

    static WebSocketModule webSocketModule(final List<WebSocketMapping> webSocketMappings,
                                           final List<EventMapping> outgoingEventMappings,
                                           final List<IncomingEventMapping> incomingEventMappings,
                                           final List<IncomingEventMapping> closeEventMappings) {
        validateNotNull(webSocketMappings, "webSocketMappings");
        validateNotNull(outgoingEventMappings, "outgoingEventMappings");
        validateNotNull(incomingEventMappings, "incomingEventMappings");
        validateNotNull(closeEventMappings, "closeEventMappings");
        final WebSocketRegistry registry = webSocketRegistry();
        return new WebSocketModule(webSocketMappings, outgoingEventMappings, incomingEventMappings, closeEventMappings, registry);
    }

    public static WebSocketModuleBuilder webSocketModule() {
        return webSocketModuleBuilder();
    }

    public WebSocketsMetrics metrics() {
        return registry.metrics();
    }

    @Override
    public void register(final ChainRegistry chainRegistry, final MessageBus messageBus) {
        createSkeleton(chainRegistry);
        chainRegistry.addProcessorToChain(WEBSOCKET_ESTABLISHMENT, createWebSocketProcessor(registry));
        chainRegistry.addProcessorToChain(DETERMINE_WEBSOCKET_TYPE, determineWebSocketTypeProcessor(webSocketMappings));
        chainRegistry.addProcessorToChain(WEBSOCKET_OPEN, activateWebSocketProcessor(registry));
        chainRegistry.addProcessorToChain(DETERMINE_EVENT, determineEventForWebSockets(outgoingEventMappings));
        chainRegistry.addProcessorToChain(WEBSOCKET_MESSAGE, handleNewWebSocketMessageProcessor(registry, outgoingEventMappings));
        chainRegistry.addProcessorToChain(SEND_TO_WEBSOCKETS, sendToWebSocketsProcessor(registry));
        chainRegistry.addProcessorToChain(WEBSOCKET_CLOSED, removeWebSocketFromRegistryProcessor(registry));
        chainRegistry.addProcessorToChain(WEBSOCKET_CLOSE, closeWebSocketProcessor(registry));
        registerIncomingEvents(messageBus, chainRegistry);
        registerCloseEvents(messageBus, chainRegistry);
    }

    private static void createSkeleton(final ChainRegistry chainRegistry) {
        final Chain exceptionChain = chainRegistry.getChainFor(EXCEPTION_OCCURRED);
        final Chain websocketEstablishmentChain = chainRegistry.createChain(
                WEBSOCKET_ESTABLISHMENT, consume(), jumpTo(exceptionChain));
        final Chain authorizationChain = chainRegistry.getChainFor(AUTHORIZATION);
        authorizationChain.addRoutingRule(jumpRule(
                websocketEstablishmentChain, metaData -> metaData.getOptional(WEBSOCKET_ID).isPresent()));
        final Chain authenticationChain = chainRegistry.getChainFor(AUTHENTICATION);
        final Chain determineWebSocketTypeChain = chainRegistry.createChain(
                DETERMINE_WEBSOCKET_TYPE, jumpTo(authenticationChain), jumpTo(exceptionChain));
        final Chain processHeadersChain = chainRegistry.getChainFor(PROCESS_HEADERS);
        processHeadersChain.addRoutingRule(
                jumpRule(determineWebSocketTypeChain, metaData -> metaData.getOptional(WEBSOCKET_ID).isPresent()));
        chainRegistry.createChain(WEBSOCKET_OPEN, consume(), jumpTo(exceptionChain));
        final Chain determineEventChain = chainRegistry.getChainFor(DETERMINE_EVENT);
        final Chain preMapToEventChain = chainRegistry.getChainFor(PRE_MAP_TO_EVENT);
        determineEventChain.addRoutingRule(jumpRule(preMapToEventChain, metaData -> metaData.contains(WEBSOCKET_ID)));
        final Chain preDetermineEventChain = chainRegistry.getChainFor(PRE_DETERMINE_EVENT);
        chainRegistry.createChain(WEBSOCKET_MESSAGE, jumpTo(preDetermineEventChain), jumpTo(exceptionChain));
        final Chain sendToWebSocketsChain = chainRegistry.createChain(
                SEND_TO_WEBSOCKETS, consume(), jumpTo(exceptionChain));
        final Chain serializationChain = chainRegistry.getChainFor(SERIALIZATION);
        serializationChain.addRoutingRule(
                jumpRule(sendToWebSocketsChain, metaData -> metaData.getOptional(IS_WEBSOCKET).orElse(false)));
        chainRegistry.createChain(WEBSOCKET_CLOSED, consume(), jumpTo(exceptionChain));
        chainRegistry.createChain(WEBSOCKET_CLOSE, consume(), jumpTo(exceptionChain));
    }

    private void registerIncomingEvents(final MessageBus messageBus,
                                        final ChainRegistry chainRegistry) {
        registerEventHandlers(incomingEventMappings, messageBus, (event, webSocket) -> {
            final MetaData metaData = emptyMetaData();
            metaData.set(IS_WEBSOCKET, true);
            metaData.set(WEBSOCKET_ID, webSocket.id());
            metaData.set(EVENT_RETURN_VALUE, of((Map) event));
            chainRegistry.putIntoChain(PRE_SERIALIZATION, metaData, metaData1 -> {
            });
        });
    }

    private void registerCloseEvents(final MessageBus messageBus,
                                     final ChainRegistry chainRegistry) {
        registerEventHandlers(closeEventMappings, messageBus, (event, webSocket) -> {
            final MetaData metaData = emptyMetaData();
            metaData.set(WEBSOCKET_ID, webSocket.id());
            chainRegistry.putIntoChain(WEBSOCKET_CLOSE, metaData, metaData1 -> {
            });
        });
    }

    private void registerEventHandlers(final List<IncomingEventMapping> eventMappings,
                                       final MessageBus messageBus,
                                       final BiConsumer<Object, WebSocket> action) {
        eventMappings.forEach(eventMapping -> {
            final EventType eventType = eventMapping.eventType();
            messageBus.subscribe(eventType, event -> registry.allActiveWebSockets().forEach(websocket -> {
                final MetaData metaData = emptyMetaData();
                websocket.savedMetaDataEntries().restoreTo(metaData);
                if (eventMapping.filter().test(metaData, event)) {
                    action.accept(event, websocket);
                }
            }));
        });
    }
}
