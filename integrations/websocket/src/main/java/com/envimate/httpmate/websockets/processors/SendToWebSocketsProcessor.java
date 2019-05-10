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

package com.envimate.httpmate.websockets.processors;

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.chains.Processor;
import com.envimate.httpmate.websockets.WebSocket;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_STRING;
import static com.envimate.httpmate.websockets.WebsocketChainKeys.RECIPIENT_WEBSOCKETS;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SendToWebSocketsProcessor implements Processor {
    //private final WebSocketRegistry registry;

    public static Processor sendToWebSocketsProcessor() {
        return new SendToWebSocketsProcessor();
    }

    @Override
    public void apply(final MetaData metaData) {
        metaData.getOptional(RESPONSE_STRING).ifPresent(message -> {
            //final WebSocketId webSocketId = metaData.get(WEBSOCKET_ID);
            //final WebSocketRegistry registry = metaData.get(WEBSOCKET_REGISTRY);
            final List<WebSocket> webSockets = metaData.get(RECIPIENT_WEBSOCKETS);
            webSockets.forEach(webSocket -> webSocket.sendText(message));
            //final WebSocket webSocket = registry.byId(webSocketId);
            //webSocket.sendText(message);
        });
    }
}
