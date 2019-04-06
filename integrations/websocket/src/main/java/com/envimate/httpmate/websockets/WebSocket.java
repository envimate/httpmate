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

import com.envimate.httpmate.websockets.registry.WebSocketId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocket {
    private final WebSocketId id;
    private final WebSocketDelegate webSocketDelegate;
    private final SavedMetaDataEntries savedMetaDataEntries;

    public static WebSocket webSocket(final WebSocketId id,
                                      final WebSocketDelegate webSocketDelegate,
                                      final SavedMetaDataEntries savedMetaDataEntries) {
        validateNotNull(id, "id");
        validateNotNull(savedMetaDataEntries, "savedMetaDataEntries");
        return new WebSocket(id,
                webSocketDelegate,
                savedMetaDataEntries);
    }

    public WebSocketId id() {
        return id;
    }

    public SavedMetaDataEntries savedMetaDataEntries() {
        return savedMetaDataEntries;
    }

    public synchronized void sendText(final String message) {
        webSocketDelegate.sendText(message);
    }

    public synchronized void close() {
        webSocketDelegate.close();
    }
}
