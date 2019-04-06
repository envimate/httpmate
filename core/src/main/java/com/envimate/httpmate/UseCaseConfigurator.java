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

package com.envimate.httpmate;

import com.envimate.httpmate.builder.configurators.InstantiationConfigurator;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.useCaseAdapter.UseCaseAdapter;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterDeserializationStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterResponseSerializationStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep3Builder;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestMapper;
import com.envimate.messageMate.useCaseAdapter.mapping.ResponseMapper;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterBuilder.anUseCaseAdapter;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UseCaseConfigurator {
    private UseCaseInstantiator useCaseInstantiator;
    private final Map<Class<?>, EventType> useCaseToEventMappings = new HashMap<>();
    private final List<Consumer<UseCaseAdapterDeserializationStep1Builder>> deserializers = new LinkedList<>();
    private RequestMapper defaultRequestMapper;
    private final List<Consumer<UseCaseAdapterResponseSerializationStep1Builder>> serializers = new LinkedList<>();
    private ResponseMapper defaultResponseMapper;

    static UseCaseConfigurator useCaseConfigurator() {
        return new UseCaseConfigurator();
    }

    InstantiationConfigurator configureUseCaseInstantiation() {
        return useCaseInstantiator -> this.useCaseInstantiator = useCaseInstantiator;
    }

    void addUseCaseToEventMapping(final Class<?> useCaseClass,
                                  final EventType eventType) {
        validateNotNull(useCaseClass, "useCaseClass");
        validateNotNull(eventType, "eventType");
        useCaseToEventMappings.put(useCaseClass, eventType);
    }

    void addRequestMapper(final BiPredicate<Class<?>, Map<String, Object>> filter,
                          final RequestMapper<?> requestMapper) {
        validateNotNull(filter, "filter");
        validateNotNull(requestMapper, "requestMapper");
        deserializers.add(deserializationStage -> deserializationStage
                .mappingRequestsToUseCaseParametersThat(filter)
                .using((RequestMapper<Object>) requestMapper));
    }

    void setDefaultRequestMapper(final RequestMapper<?> requestMapper) {
        validateNotNull(requestMapper, "requestMapper");
        this.defaultRequestMapper = requestMapper;
    }

    void addResponseSerializer(final Predicate<Object> filter,
                               final ResponseMapper<Object> responseMapper) {
        validateNotNull(filter, "filter");
        validateNotNull(responseMapper, "responseMapper");
        serializers.add(serializationStage -> serializationStage
                .serializingResponseObjectsThat(filter)
                .using(responseMapper));
    }

    void addResponseSerializer(final Consumer<UseCaseAdapterResponseSerializationStep1Builder> consumer) {
        validateNotNull(consumer, "consumer");
        serializers.add(consumer);
    }

    void setDefaultResponseSerializer(final ResponseMapper<?> defaultResponseMapper) {
        this.defaultResponseMapper = defaultResponseMapper;
    }

    HttpMate build(final HttpMateConfigurator configurator) {
        final UseCaseAdapterStep1Builder useCaseAdapterBuilder = anUseCaseAdapter();

        useCaseToEventMappings.forEach((useCase, eventType) -> {
            final UseCaseAdapterStep3Builder<?> useCaseAdapterStep3Builder = useCaseAdapterBuilder
                    .invokingUseCase(useCase)
                    .forType(eventType);
            useCaseAdapterStep3Builder.callingTheSingleUseCaseMethod();
        });

        final UseCaseAdapterDeserializationStep1Builder useCaseAdapterDeserializationStep1Builder =
                ofNullable(useCaseInstantiator)
                        .map(useCaseAdapterBuilder::obtainingUseCaseInstancesUsing)
                        .orElseGet(useCaseAdapterBuilder::obtainingUseCaseInstancesUsingTheZeroArgumentConstructor);

        deserializers.forEach(deserializer -> deserializer.accept(useCaseAdapterDeserializationStep1Builder));
        final UseCaseAdapterResponseSerializationStep1Builder useCaseAdapterResponseSerializationStep1Builder =
                useCaseAdapterDeserializationStep1Builder
                        .mappingRequestsToUseCaseParametersByDefaultUsing(defaultRequestMapper);

        serializers.forEach(serializer -> serializer.accept(useCaseAdapterResponseSerializationStep1Builder));
        final UseCaseAdapter useCaseAdapter = useCaseAdapterResponseSerializationStep1Builder
                .serializingResponseObjectsByDefaultUsing(defaultResponseMapper)
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault();

        final MessageBus messageBus = configurator.getMessageBus();
        useCaseAdapter.attachTo(messageBus);
        return configurator.build(messageBus);
    }
}
