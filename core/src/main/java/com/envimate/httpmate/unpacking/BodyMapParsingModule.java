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

package com.envimate.httpmate.unpacking;

import com.envimate.httpmate.Module;
import com.envimate.httpmate.chains.ChainRegistry;
import com.envimate.httpmate.request.ContentType;
import com.envimate.messageMate.messageBus.MessageBus;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.envimate.httpmate.chains.HttpMateChainKeys.*;
import static com.envimate.httpmate.chains.HttpMateChains.PRE_DETERMINE_EVENT;
import static com.envimate.httpmate.unpacking.BodyMapParsingModuleBuilder.bodyMapParsingModuleBuilder;
import static com.envimate.httpmate.unpacking.UnsupportedContentTypeException.unsupportedContentTypeException;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BodyMapParsingModule implements Module {
    private final ContentType defaultContentType;
    private final Map<ContentType, Function<String, Map<String, Object>>> bodyParsers;

    public static BodyMapParsingModuleBuilder aBodyMapParsingModule() {
        return bodyMapParsingModuleBuilder();
    }

    static Module bodyMapParsingModule(final ContentType defaultContentType,
                                       final Map<ContentType, Function<String, Map<String, Object>>> bodyParsers) {
        validateNotNull(defaultContentType, "defaultContentType");
        validateNotNull(bodyParsers, "bodyParsers");
        if (!bodyParsers.containsKey(defaultContentType)) {
            throw new RuntimeException(format(
                    "No parser for default content type '%s' configured", defaultContentType.internalValueForMapping()
            ));
        }
        return new BodyMapParsingModule(defaultContentType, bodyParsers);
    }

    @Override
    public void register(final ChainRegistry chainRegistry,
                         final MessageBus messageBus) {
        chainRegistry.addProcessorToChain(PRE_DETERMINE_EVENT, metaData ->
                metaData.getOptional(BODY_STRING).ifPresent(body -> {
                    final ContentType contentType = metaData.get(CONTENT_TYPE);

                    final Function<String, Map<String, Object>> bodyParser;
                    if (contentType.isEmpty()) {
                        bodyParser = bodyParsers.get(defaultContentType);
                    } else {
                        bodyParser = bodyParsers.get(contentType);
                    }

                    if (isNull(bodyParser)) {
                        throw unsupportedContentTypeException(contentType, bodyParsers.keySet());
                    }
                    final Map<String, Object> mapBody = ofNullable(bodyParser.apply(body))
                            .orElseGet(HashMap::new);

                    metaData.set(BODY_MAP, mapBody);
                }));
    }
}
