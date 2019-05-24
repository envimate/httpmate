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

package com.envimate.httpmate.usecases;

import com.envimate.httpmate.chains.ChainExtender;
import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.usecases.usecase.SerializerAndDeserializer;
import com.envimate.messageMate.mapping.Demapifier;
import com.envimate.messageMate.mapping.Mapifier;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseAdapter;
import com.envimate.messageMate.useCases.building.DeserializationStep1Builder;
import com.envimate.messageMate.useCases.building.ResponseSerializationStep1Builder;
import com.envimate.messageMate.useCases.building.Step1Builder;
import com.envimate.messageMate.useCases.building.Step3Builder;
import com.envimate.messageMate.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
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

import static com.envimate.httpmate.events.EventModule.MESSAGE_BUS;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvocationBuilder.anUseCaseAdapter;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UseCasesModule implements ChainModule {
    private UseCaseInstantiator useCaseInstantiator;
    private final Map<Class<?>, EventType> useCaseToEventMappings = new HashMap<>();
    private final List<Consumer<DeserializationStep1Builder>> deserializers = new LinkedList<>();
    private final List<Consumer<ResponseSerializationStep1Builder>> serializers = new LinkedList<>();
    private SerializerAndDeserializer serializerAndDeserializer;

    private Demapifier<Object> defaultRequestMapper;
    private Mapifier<Object> defaultResponseMapper;

    public static UseCasesModule useCasesModule() {
        return new UseCasesModule();
    }

    public void setUseCaseInstantiator(final UseCaseInstantiator useCaseInstantiator) {
        this.useCaseInstantiator = useCaseInstantiator;
    }

    public void addUseCaseToEventMapping(final Class<?> useCaseClass,
                                         final EventType eventType) {
        validateNotNull(useCaseClass, "useCaseClass");
        validateNotNull(eventType, "eventType");
        useCaseToEventMappings.put(useCaseClass, eventType);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addRequestMapper(final EventFilter filter,
                                 final Demapifier<?> requestMapper) {
        validateNotNull(filter, "filter");
        validateNotNull(requestMapper, "requestMapper");
        deserializers.add(deserializationStage -> deserializationStage
                .mappingRequestsToUseCaseParametersThat((BiPredicate<Class<?>, Map<String, Object>>) filter::filter)
                .using((Demapifier<Object>) requestMapper));
    }

    public void setDefaultRequestMapper(final Demapifier<Object> requestMapper) {
        validateNotNull(requestMapper, "requestMapper");
        this.defaultRequestMapper = requestMapper;
    }

    public void addResponseSerializer(final Predicate<Object> filter,
                                      final Mapifier<Object> responseMapper) {
        validateNotNull(filter, "filter");
        validateNotNull(responseMapper, "responseMapper");
        serializers.add(serializationStage -> serializationStage
                .serializingResponseObjectsThat(filter)
                .using(responseMapper));
    }

    public void setDefaultResponseSerializer(final Mapifier<Object> defaultResponseMapper) {
        this.defaultResponseMapper = defaultResponseMapper;
    }

    void setSerializerAndDeserializer(final SerializerAndDeserializer serializerAndDeserializer) {
        this.serializerAndDeserializer = serializerAndDeserializer;
    }

    @Override
    public void register(final ChainExtender extender) {
        serializerAndDeserializer.register(extender);

        final Step1Builder useCaseAdapterBuilder = anUseCaseAdapter();

        useCaseToEventMappings.forEach((useCase, eventType) -> {
            final Step3Builder<?> useCaseAdapterStep3Builder = useCaseAdapterBuilder
                    .invokingUseCase(useCase)
                    .forType(eventType);
            useCaseAdapterStep3Builder.callingTheSingleUseCaseMethod();
        });

        final DeserializationStep1Builder useCaseAdapterDeserializationStep1Builder =
                ofNullable(useCaseInstantiator)
                        .map(useCaseAdapterBuilder::obtainingUseCaseInstancesUsing)
                        .orElseGet(useCaseAdapterBuilder::obtainingUseCaseInstancesUsingTheZeroArgumentConstructor);

        deserializers.forEach(deserializer -> deserializer.accept(useCaseAdapterDeserializationStep1Builder));
        final ResponseSerializationStep1Builder useCaseAdapterResponseSerializationStep1Builder =
                useCaseAdapterDeserializationStep1Builder
                        .deserializeObjectsPerDefault(defaultRequestMapper);

        serializers.forEach(serializer -> serializer.accept(useCaseAdapterResponseSerializationStep1Builder));
        final UseCaseAdapter useCaseAdapter = useCaseAdapterResponseSerializationStep1Builder
                .serializingObjectsByDefaultUsing(defaultResponseMapper)
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault()
                //TODO: is that the right build call???
                .buildAsStandaloneAdapter();

        final MessageBus messageBus = extender.getMetaDatum(MESSAGE_BUS);
        useCaseAdapter.attachTo(messageBus);
    }
}
