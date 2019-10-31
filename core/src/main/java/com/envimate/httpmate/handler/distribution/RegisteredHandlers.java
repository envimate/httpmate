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

package com.envimate.httpmate.handler.distribution;

import com.envimate.httpmate.chains.MetaDataKey;
import com.envimate.httpmate.generator.GenerationCondition;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.envimate.httpmate.chains.MetaDataKey.metaDataKey;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.stream.Collectors.toMap;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegisteredHandlers {
    public static final MetaDataKey<RegisteredHandlers> REGISTERED_HANDLERS = metaDataKey("REGISTERED_HANDLERS");

    private final Map<GenerationCondition, Object> handlers;

    public static RegisteredHandlers registeredHandlers() {
        return new RegisteredHandlers(new HashMap<>());
    }

    public void register(final GenerationCondition condition, final Object handler) {
        validateNotNull(condition, "condition");
        validateNotNull(handler, "handler");
        handlers.put(condition, handler);
    }

    public Map<GenerationCondition, Object> allHandlersThat(final Predicate<Object> predicate) {
        return handlers.entrySet().stream()
                .filter(entry -> predicate.test(entry.getValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
