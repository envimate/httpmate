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

package com.envimate.httpmate.convenience.configurators.exceptions;

import com.envimate.httpmate.CoreModule;
import com.envimate.httpmate.chains.Configurator;
import com.envimate.httpmate.exceptions.ExceptionMapper;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionMappingConfigurator {
    private final Map<Class<? extends Throwable>, ExceptionMapper<? extends Throwable>> mappers = new HashMap<>();

    public static ExceptionMappingConfigurator toMapExceptions() {
        return new ExceptionMappingConfigurator();
    }

    public <T extends Throwable> ToResponsesUsingStage<T> ofType(final Class<T> type) {
        validateNotNull(type, "type");
        return mapper -> {
            validateNotNull(mapper, "mapper");
            ExceptionMappingConfigurator.this.mappers.put(type, mapper);
            return this;
        };
    }

    @SuppressWarnings("unchecked")
    public Configurator ofAllRemainingTypesUsing(final ExceptionMapper<Throwable> mapper) {
        validateNotNull(mapper, "mapper");
        return dependencyRegistry -> {
            final CoreModule coreModule = dependencyRegistry.getDependency(CoreModule.class);
            mappers.forEach((type, responseMapper) ->
                    coreModule.addExceptionMapper(areOfType(type), (ExceptionMapper<Throwable>) responseMapper));
            coreModule.setDefaultExceptionMapper(mapper);
        };
    }

    private static Predicate<Throwable> areOfType(final Class<? extends Throwable> type) {
        return type::isInstance;
    }
}
