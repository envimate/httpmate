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
import com.envimate.httpmate.chains.DependencyRegistry;
import com.envimate.httpmate.http.headers.ContentType;
import com.envimate.httpmate.marshalling.MarshallingModule;
import com.envimate.httpmate.usecases.usecase.SerializerAndDeserializer;
import com.envimate.mapmate.builder.MapMate;
import com.envimate.mapmate.marshalling.MarshallingType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.envimate.httpmate.marshalling.MarshallingModule.emptyMarshallingModule;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Map.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMateSerializerAndDeserializer implements SerializerAndDeserializer {
    private static final Map<MarshallingType, ContentType> DEFAULT_CONTENT_TYPE_MAPPINGS = of(
            MarshallingType.json(), ContentType.json(),
            MarshallingType.xml(), ContentType.xml(),
            MarshallingType.yaml(), ContentType.yaml(),
            MarshallingType.urlEncoded(), ContentType.formUrlEncoded()
    );
    private static final List<MarshallingType> DEFAULT_SUPPORTED_TYPES_FOR_UNMARSHALLING = asList(
            MarshallingType.json(), MarshallingType.xml(), MarshallingType.yaml(), MarshallingType.urlEncoded());
    private static final List<MarshallingType> DEFAULT_SUPPORTED_TYPES_FOR_MARSHALLING = asList(
            MarshallingType.json(), MarshallingType.xml(), MarshallingType.yaml());

    private MapMate mapMate;
    private ContentType defaultContentType;

    private final Map<ContentType, MarshallingType> contentTypeMappingsForUnmarshalling = new HashMap<>();
    private final Map<ContentType, MarshallingType> contentTypeMappingsForMarshalling = new HashMap<>();

    public static MapMateIntegrationBuilder mapMateIntegration(final MapMate mapMate) {
        return MapMateIntegrationBuilder.mapMateIntegration(mapMate);
    }

    static MapMateSerializerAndDeserializer mapMateSerializerAndDeserializer() {
        return new MapMateSerializerAndDeserializer();
    }

    public void setMapMate(final MapMate mapMate) {
        validateNotNull(mapMate, "mapMate");
        this.mapMate = mapMate;
    }

    public void setDefaultContentType(final ContentType defaultContentType) {
        validateNotNull(defaultContentType, "defaultContentType");
        this.defaultContentType = defaultContentType;
    }

    public void addRequestContentTypeToUnmarshallingTypeMapping(final ContentType contentType,
                                                                final MarshallingType marshallingType) {
        validateNotNull(contentType, "contentType");
        validateNotNull(marshallingType, "marshallingType");
        contentTypeMappingsForUnmarshalling.put(contentType, marshallingType);
    }

    public void addMarshallingTypeToResponseContentTypeMapping(final ContentType contentType,
                                                               final MarshallingType marshallingType) {
        validateNotNull(contentType, "contentType");
        validateNotNull(marshallingType, "marshallingType");
        contentTypeMappingsForMarshalling.put(contentType, marshallingType);
    }

    @Override
    public <T> T deserialize(final Class<T> type, final Map<String, Object> map) {
        return mapMate.deserializer().deserializeFromMap(map, type);
    }

    @Override
    public Map<String, Object> serialize(final Object event) {
        return mapMate.serializer().serializeToMap(event);
    }

    @Override
    public List<ChainModule> supplyModulesIfNotAlreadyPreset() {
        return singletonList(emptyMarshallingModule());
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        mapMate.deserializer().supportedMarshallingTypes().stream()
                .filter(marshallingType -> !contentTypeMappingsForUnmarshalling.containsValue(marshallingType))
                .filter(DEFAULT_SUPPORTED_TYPES_FOR_UNMARSHALLING::contains)
                .forEach(marshallingType -> {
                    final ContentType contentType = DEFAULT_CONTENT_TYPE_MAPPINGS.get(marshallingType);
                    contentTypeMappingsForUnmarshalling.put(contentType, marshallingType);
                });

        final MarshallingModule marshallingModule = dependencyRegistry.getDependency(MarshallingModule.class);
        contentTypeMappingsForUnmarshalling.forEach((contentType, marshallingType) -> marshallingModule
                .addUnmarshaller(contentType, input -> mapMate.deserializer().deserializeToMap(input, marshallingType)));

        mapMate.serializer().supportedMarshallingTypes().stream()
                .filter(marshallingType -> !contentTypeMappingsForMarshalling.containsValue(marshallingType))
                .filter(DEFAULT_SUPPORTED_TYPES_FOR_MARSHALLING::contains)
                .forEach(marshallingType -> {
                    final ContentType contentType = DEFAULT_CONTENT_TYPE_MAPPINGS.get(marshallingType);
                    contentTypeMappingsForMarshalling.put(contentType, marshallingType);
                });
        contentTypeMappingsForMarshalling.forEach((contentType, marshallingType) -> marshallingModule
                .addMarshaller(contentType, map -> mapMate.serializer().serializeFromMap(map, marshallingType)));
    }
}
