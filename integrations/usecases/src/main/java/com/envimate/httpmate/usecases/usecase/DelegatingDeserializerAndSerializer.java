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

package com.envimate.httpmate.usecases.usecase;

import com.envimate.messageMate.useCaseAdapter.mapping.RequestMapper;
import com.envimate.messageMate.useCaseAdapter.mapping.ResponseMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DelegatingDeserializerAndSerializer implements SerializerAndDeserializer {
    private final RequestMapper<Object> requestMapper;
    private final ResponseMapper<Object> responseMapper;

    public static SerializerAndDeserializer delegatingDeserializerAndSerializer(final RequestMapper<Object> requestMapper,
                                                                                final ResponseMapper<Object> responseMapper) {
        validateNotNull(requestMapper, "requestMapper");
        validateNotNull(responseMapper, "responseMapper");
        return new DelegatingDeserializerAndSerializer(requestMapper, responseMapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(final Class<T> type,
                             final Map<String, Object> map) {
        return (T) requestMapper.map((Class<Object>) type, map);
    }

    @Override
    public Map<String, Object> serialize(final Object event) {
        return responseMapper.map(event);
    }
}
