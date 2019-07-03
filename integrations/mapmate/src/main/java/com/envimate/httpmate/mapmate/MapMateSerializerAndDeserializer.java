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

package com.envimate.httpmate.mapmate;

import com.envimate.httpmate.HttpMateChainKeys;
import com.envimate.httpmate.chains.ChainExtender;
import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.chains.DependencyRegistry;
import com.envimate.httpmate.events.EventModule;
import com.envimate.httpmate.http.ContentType;
import com.envimate.httpmate.usecases.usecase.SerializerAndDeserializer;
import com.envimate.mapmate.builder.MapMate;
import com.envimate.mapmate.marshalling.MarshallingType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_HEADERS;
import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_STRING;
import static com.envimate.httpmate.http.Http.Headers.CONTENT_TYPE;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMateSerializerAndDeserializer implements SerializerAndDeserializer {
    private final MapMate mapMate;
    private final ChainModule bodyMapParsingModule;
    private final ContentType defaultContentType;
    private final Map<ContentType, MarshallingType> marshallingTypes;

    public static MapMateIntegrationBuilder mapMateIntegration(final MapMate mapMate) {
        return MapMateIntegrationBuilder.mapMateIntegration(mapMate);
    }

    static MapMateSerializerAndDeserializer mapMateSerializerAndDeserializer(
            final MapMate mapMate,
            final ChainModule bodyMapParsingModule,
            final ContentType defaultContentType,
            final Map<ContentType, MarshallingType> marshallingTypes) {
        validateNotNull(mapMate, "mapMate");
        validateNotNull(bodyMapParsingModule, "bodyMapParsingModule");
        validateNotNull(defaultContentType, "defaultContentType");
        validateNotNull(marshallingTypes, "marshallingTypes");
        return new MapMateSerializerAndDeserializer(
                mapMate, bodyMapParsingModule, defaultContentType, marshallingTypes);
    }

    @Override
    public <T> T deserialize(final Class<T> type, final Map<String, Object> map) {
        return mapMate.deserializer().deserializeFromMap(map, type);
    }

    @Override
    public Map<String, Object> serialize(final Object event) {
        return mapMate.serializer().serializeToMap(event);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        SerializerAndDeserializer.super.configure(dependencyRegistry);
        final EventModule eventModule = dependencyRegistry.getDependency(EventModule.class);
        eventModule.setResponseMapper((event, metaData) -> {
            final ContentType contentType = metaData.get(HttpMateChainKeys.CONTENT_TYPE);
            final ContentType responseContentType;
            if (!contentType.isEmpty()) {
                responseContentType = contentType;
            } else {
                responseContentType = defaultContentType;
            }
            metaData.get(RESPONSE_HEADERS).put(CONTENT_TYPE, responseContentType.internalValueForMapping());
            final MarshallingType marshallingType = marshallingTypes.get(responseContentType);
            final Map<String, Object> eventMap = (Map<String, Object>) event;
            metaData.set(RESPONSE_STRING, mapMate.serializer().serializeFromMap(eventMap, marshallingType));
        });
    }

    @Override
    public void register(final ChainExtender extender) {
        bodyMapParsingModule.register(extender);
    }
}
