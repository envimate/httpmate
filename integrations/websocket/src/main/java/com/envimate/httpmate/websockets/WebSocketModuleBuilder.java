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

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.chains.MetaDataKey;
import com.envimate.httpmate.websockets.builder.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.envimate.httpmate.chains.HttpMateChainKeys.*;
import static com.envimate.httpmate.path.PathTemplate.pathTemplate;
import static com.envimate.httpmate.websockets.EventMapping.eventMapping;
import static com.envimate.httpmate.websockets.IncomingEventMapping.incomingEventMapping;
import static com.envimate.httpmate.websockets.MetaDataEntriesToSave.metaDataEntriesToSave;
import static com.envimate.httpmate.websockets.MetaDataEntryProvider.storing;
import static com.envimate.httpmate.websockets.WebSocketMapping.webSocketMapping;
import static com.envimate.httpmate.websockets.WebSocketModule.webSocketModule;
import static com.envimate.messageMate.messageBus.EventType.eventTypeFromString;
import static java.util.Arrays.asList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketModuleBuilder implements EventStage {
    private final List<WebSocketMapping> webSocketMappings = new LinkedList<>();
    private final List<EventMapping> outgoingEventMappings = new LinkedList<>();
    private final List<IncomingEventMapping> incomingEventMappings = new LinkedList<>();
    private final List<IncomingEventMapping> closeEventMappings = new LinkedList<>();

    static WebSocketModuleBuilder webSocketModuleBuilder() {
        return new WebSocketModuleBuilder();
    }

    public CategorizerStage acceptingWebSocketsToThePath(final String template) {
        return new CategorizerStage() {
            @Override
            public <T> WebSocketModuleBuilder initializingMetaDataForIncomingMessagesWith(
                    final MetaDataKey<T> key,
                    final Function<MetaData, T> valueProvider) {
                final MetaDataEntryProvider provider = storing(key, valueProvider);
                final List<MetaDataEntryProvider> providers = asList(
                        MetaDataEntryProvider.saving(PATH_PARAMETERS),
                        MetaDataEntryProvider.saving(QUERY_PARAMETERS),
                        MetaDataEntryProvider.saving(HEADERS),
                        MetaDataEntryProvider.saving(CONTENT_TYPE),
                        provider);
                final MetaDataEntriesToSave metaDataEntriesToSave = metaDataEntriesToSave(providers);
                webSocketMappings.add(webSocketMapping(metaDataEntriesToSave, pathTemplate(template)));
                return WebSocketModuleBuilder.this;
            }
        };
    }

    @Override
    public When<EventStage> choosingTheEvent(final String eventType) {
        return predicate -> {
            final EventMapping eventMapping = eventMapping(eventTypeFromString(eventType), predicate);
            outgoingEventMappings.add(eventMapping);
            return this;
        };
    }

    @Override
    public <E> To<EventStage, E> forwardingTheEvent(final String eventType) {
        return filter -> {
            final IncomingEventMapping eventMapping = incomingEventMapping(eventTypeFromString(eventType), filter);
            incomingEventMappings.add(eventMapping);
            return this;
        };
    }

    @Override
    public <E> That<EventStage, E> closingOn(final String eventType) {
        return filter -> {
            final IncomingEventMapping eventMapping = incomingEventMapping(eventTypeFromString(eventType), filter);
            closeEventMappings.add(eventMapping);
            return this;
        };
    }

    @Override
    public WebSocketModule build() {
        return webSocketModule(webSocketMappings, outgoingEventMappings, incomingEventMappings, closeEventMappings);
    }
}
