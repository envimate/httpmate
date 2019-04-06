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

package com.envimate.httpmate.websockets.registry;

import com.envimate.httpmate.websockets.WebSocket;
import com.envimate.httpmate.websockets.WebSocketsMetrics;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Set;

import static com.envimate.httpmate.websockets.WebSocketsMetrics.webSocketsMetrics;
import static com.envimate.httpmate.websockets.registry.SaveMap.saveMap;
import static com.envimate.httpmate.websockets.registry.WebSocketNotFoundException.webSocketNotFoundException;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketRegistry {
    private final SaveMap<WebSocketId, WebSocket> preActiveWebSockets;
    private final SaveMap<WebSocketId, WebSocket> activeWebSockets;

    public static WebSocketRegistry webSocketRegistry() {
        return new WebSocketRegistry(saveMap(), saveMap());
    }

    public synchronized WebSocket byId(final WebSocketId id) {
        return activeWebSockets.get(id).orElseThrow(() -> webSocketNotFoundException(id));
    }

    public synchronized void register(final WebSocketId id,
                                      final WebSocket webSocket) {
        preActiveWebSockets.put(id, webSocket);
    }

    public synchronized void activate(final WebSocketId id) {
        final WebSocket webSocket = preActiveWebSockets.getAndRemove(id).orElseThrow(() -> webSocketNotFoundException(id));
        activeWebSockets.put(id, webSocket);
    }

    public synchronized void unregister(final WebSocketId id) {
        activeWebSockets.getAndRemove(id).orElseThrow(() -> webSocketNotFoundException(id));
    }

    public synchronized Set<WebSocket> allActiveWebSockets() {
        return activeWebSockets.copyOfValues();
    }

    public synchronized WebSocketsMetrics metrics() {
        return webSocketsMetrics(activeWebSockets.size() + preActiveWebSockets.size());
    }
}
