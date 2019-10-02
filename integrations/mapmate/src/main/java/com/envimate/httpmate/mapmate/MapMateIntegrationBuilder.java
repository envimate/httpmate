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

import com.envimate.httpmate.chains.ConfiguratorBuilder;
import com.envimate.httpmate.http.headers.ContentType;
import com.envimate.httpmate.mapmate.builder.MarshallerTypeStage;
import com.envimate.mapmate.builder.MapMate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.mapmate.MapMateSerializerAndDeserializer.mapMateSerializerAndDeserializer;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMateIntegrationBuilder implements ConfiguratorBuilder {
    private final MapMateSerializerAndDeserializer mapMateSerializerAndDeserializer;

    static MapMateIntegrationBuilder mapMateIntegration(final MapMate mapMate) {
        validateNotNull(mapMate, "mapMate");
        final MapMateSerializerAndDeserializer mapMateSerializerAndDeserializer = mapMateSerializerAndDeserializer();
        mapMateSerializerAndDeserializer.setMapMate(mapMate);
        return new MapMateIntegrationBuilder(mapMateSerializerAndDeserializer);
    }

    public MarshallerTypeStage<MapMateIntegrationBuilder> matchingTheContentType(final ContentType contentType) {
        validateNotNull(contentType, "contentType");
        return marshallingType -> {
            validateNotNull(marshallingType, "marshallingType");
            mapMateSerializerAndDeserializer.addRequestContentTypeToUnmarshallingTypeMapping(contentType, marshallingType);
            mapMateSerializerAndDeserializer.addMarshallingTypeToResponseContentTypeMapping(contentType, marshallingType);
            return this;
        };
    }

    public MapMateIntegrationBuilder assumingTheDefaultContentType(final ContentType defaultContentType) {
        validateNotNull(defaultContentType, "defaultContentType");
        mapMateSerializerAndDeserializer.setDefaultContentType(defaultContentType);
        return this;
    }

    @Override
    public MapMateSerializerAndDeserializer build() {
        return mapMateSerializerAndDeserializer;
    }
}
