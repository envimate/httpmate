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
import com.envimate.httpmate.mapmate.builder.MarshallerTypeStage;
import com.envimate.httpmate.mapmate.builder.SerializerStage;
import com.envimate.httpmate.http.ContentType;
import com.envimate.httpmate.unpacking.BodyMapParsingModuleBuilder;
import com.envimate.mapmate.marshalling.MarshallingType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

import static com.envimate.httpmate.mapmate.MapMateSerializerAndDeserializer.mapMateSerializerAndDeserializer;
import static com.envimate.httpmate.unpacking.BodyMapParsingModule.aBodyMapParsingModule;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapMateIntegrationBuilder {
    private final Map<ContentType, MarshallingType> contentTypeMappings = new HashMap<>();

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

    public MapMateIntegrationBuilder mappingAllStandardContentTypes() {
        matchingTheContentType(ContentType.json()).toTheMarshallerType(MarshallingType.json());
        matchingTheContentType(ContentType.xml()).toTheMarshallerType(MarshallingType.xml());
        matchingTheContentType(ContentType.yaml()).toTheMarshallerType(MarshallingType.yaml());
        return this;
    }

    public SerializerStage<MapMateSerializerAndDeserializer> assumingTheDefaultContentType(final ContentType defaultContentType) {
        return serializer -> deserializer -> {
            final BodyMapParsingModuleBuilder builder = aBodyMapParsingModule();
            contentTypeMappings.forEach((contentType, marshallingType) ->
                    builder.parsingContentType(contentType)
                            .with(input -> deserializer.deserializeToMap(input, marshallingType)));
            final ChainModule bodyMapParsingModule = builder.usingTheDefaultContentType(defaultContentType);
            return mapMateSerializerAndDeserializer(
                    deserializer, serializer, bodyMapParsingModule, defaultContentType, contentTypeMappings);
        };
    }
}
