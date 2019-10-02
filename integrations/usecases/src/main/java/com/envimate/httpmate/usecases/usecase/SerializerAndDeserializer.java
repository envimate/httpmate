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

import com.envimate.httpmate.chains.ChainExtender;
import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.chains.DependencyRegistry;
import com.envimate.messageMate.mapping.Demapifier;
import com.envimate.messageMate.mapping.Mapifier;

import java.util.Map;

public interface SerializerAndDeserializer extends Demapifier<Object>, Mapifier<Object>, ChainModule {

    @Override
    default Object map(final Class<Object> targetType, final Map<String, Object> map) {
        return deserialize(targetType, map);
    }

    @Override
    default Map<String, Object> map(final Object object) {
        return serialize(object);
    }

    @Override
    default void configure(final DependencyRegistry dependencyRegistry) {
    }

    @Override
    default void register(final ChainExtender extender) {
    }

    <T> T deserialize(Class<T> type, Map<String, Object> map);

    Map<String, Object> serialize(Object event);
}
