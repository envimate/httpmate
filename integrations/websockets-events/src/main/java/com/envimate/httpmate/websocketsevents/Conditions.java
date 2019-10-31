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

import com.envimate.httpmate.events.ExternalEventMapping;
import com.envimate.httpmate.generator.GenerationCondition;
import com.envimate.httpmate.websockets.WebSocketForEventFilter;
import com.envimate.httpmate.websockets.WebSocketTag;

import static com.envimate.httpmate.events.EventsChains.MAP_EVENT_TO_RESPONSE;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static com.envimate.httpmate.websockets.WebSocketTag.webSocketTag;
import static com.envimate.httpmate.websockets.WebsocketChainKeys.*;
import static com.envimate.httpmate.websockets.WebsocketChains.WEBSOCKET_CLOSE;
import static com.envimate.httpmate.websocketsevents.WebSocketsExternalEventMapping.webSocketsExternalEventMapping;

public final class Conditions {

    private Conditions() {
    }

    public static GenerationCondition webSocketIsTaggedWith(final String tag) {
        validateNotNullNorEmpty(tag, "tag");
        final WebSocketTag webSocketTag = webSocketTag(tag);
        return metaData -> metaData.getOptional(WEBSOCKET_TAG)
                .map(webSocketTag::equals)
                .orElse(false);
    }

    public static ExternalEventMapping forwardingItToAllWebSocketsThat(final WebSocketForEventFilter filter) {
        validateNotNull(filter, "filter");
        return webSocketsExternalEventMapping(MAP_EVENT_TO_RESPONSE, filter, (webSockets, metaData) -> {
            metaData.set(RECIPIENT_WEBSOCKETS, webSockets);
            metaData.set(IS_WEBSOCKET_MESSAGE, true);
        });
    }

    public static ExternalEventMapping closingAllWebSockets() {
        return closingAllWebSocketsThat((metaData, event) -> true);
    }

    public static ExternalEventMapping closingAllWebSocketsThat(final WebSocketForEventFilter filter) {
        validateNotNull(filter, "filter");
        return webSocketsExternalEventMapping(WEBSOCKET_CLOSE, filter,
                (webSockets, metaData) -> metaData.set(WEBSOCKETS_TO_CLOSE, webSockets));
    }
}
