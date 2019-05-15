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

import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.http.ContentType;
import com.envimate.httpmate.mapmate.builder.DeserializerStage;
import com.envimate.httpmate.mapmate.builder.MarshallerTypeStage;
import com.envimate.httpmate.unpacking.BodyMapParsingModule;
import com.envimate.mapmate.marshalling.MarshallingType;
import com.envimate.mapmate.serialization.Serializer;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.envimate.httpmate.mapmate.MapMateSerializerAndDeserializer.mapMateSerializerAndDeserializer;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Map.of;
import static java.util.stream.Collectors.toMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMateIntegrationBuilder {
    private static final Map<MarshallingType, ContentType> DEFAULT_CONTENT_TYPE_MAPPINGS = of(
            MarshallingType.json(), ContentType.json(),
            MarshallingType.xml(), ContentType.xml(),
            MarshallingType.yaml(), ContentType.yaml()
    );
    private final Map<ContentType, MarshallingType> contentTypeMappings = new HashMap<>();
    private ContentType defaultContentType = ContentType.json();

    static MapMateIntegrationBuilder mapMate() {
        return new MapMateIntegrationBuilder();
    }

    public MarshallerTypeStage<MapMateIntegrationBuilder> matchingTheContentType(final ContentType contentType) {
        validateNotNull(contentType, "contentType");
        return marshallingType -> {
            validateNotNull(marshallingType, "marshallingType");
            contentTypeMappings.put(contentType, marshallingType);
            return this;
        };
    }

    public MapMateIntegrationBuilder assumingTheDefaultContentType(final ContentType defaultContentType) {
        validateNotNull(defaultContentType, "defaultContentType");
        this.defaultContentType = defaultContentType;
        return this;
    }

    public DeserializerStage<MapMateSerializerAndDeserializer> usingTheSerializer(final Serializer serializer) {
        return deserializer -> {
            final Set<MarshallingType> supportedMarshallingTypes = deserializer.supportedMarshallingTypes();
            for (final MarshallingType supportedMarshallingType : supportedMarshallingTypes) {
                if (!contentTypeMappings.values().contains(supportedMarshallingType)) {
                    if (DEFAULT_CONTENT_TYPE_MAPPINGS.containsKey(supportedMarshallingType)) {
                        contentTypeMappings.put(
                                DEFAULT_CONTENT_TYPE_MAPPINGS.get(supportedMarshallingType),
                                supportedMarshallingType);
                    }
                }
            }

            final Map<ContentType, Function<String, Map<String, Object>>> bodyParsers = contentTypeMappings.entrySet()
                    .stream()
                    .collect(toMap(Map.Entry::getKey, entry -> {
                        final MarshallingType marshallingType = entry.getValue();
                        return (Function<String, Map<String, Object>>) input
                                -> deserializer.deserializeToMap(input, marshallingType);
                    }));
            final ChainModule bodyMapParsingModule = BodyMapParsingModule.bodyMapParsingModule(defaultContentType, bodyParsers);
            return mapMateSerializerAndDeserializer(
                    deserializer, serializer, bodyMapParsingModule, defaultContentType, contentTypeMappings);
        };
    }
}
