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
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.chains.MetaDataKey;
import com.envimate.httpmate.handler.distribution.HandlerDistributors;
import com.envimate.httpmate.usecases.serializing.SerializerAndDeserializer;
import com.envimate.messageMate.mapping.Demapifier;
import com.envimate.messageMate.mapping.Mapifier;
import com.envimate.messageMate.mapping.SerializationFilters;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.serializedMessageBus.SerializedMessageBus;
import com.envimate.messageMate.useCases.building.DeserializationStep1Builder;
import com.envimate.messageMate.useCases.building.ResponseSerializationStep1Builder;
import com.envimate.messageMate.useCases.building.Step1Builder;
import com.envimate.messageMate.useCases.building.Step3Builder;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseAdapter;
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

import static com.envimate.httpmate.chains.MetaDataKey.metaDataKey;
import static com.envimate.httpmate.handler.distribution.HandlerDistributors.HANDLER_DISTRIBUTORS;
import static com.envimate.httpmate.events.EventModule.MESSAGE_BUS;
import static com.envimate.httpmate.events.EventModule.eventModule;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.messageMate.mapping.DeserializationFilters.areOfType;
import static com.envimate.messageMate.processingContext.EventType.eventTypeFromClass;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvocationBuilder.anUseCaseAdapter;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UseCasesModule implements ChainModule {
    public static final MetaDataKey<SerializedMessageBus> SERIALIZED_MESSAGE_BUS = metaDataKey("SERIALIZED_MESSAGE_BUS");

    private UseCaseInstantiator useCaseInstantiator;
    private final Map<Class<?>, EventType> useCaseToEventMappings = new HashMap<>();
    private final List<Consumer<DeserializationStep1Builder>> deserializers = new LinkedList<>();
    private final List<Consumer<ResponseSerializationStep1Builder>> serializers = new LinkedList<>();
    private SerializerAndDeserializer serializerAndDeserializer;

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

    @SuppressWarnings("unchecked")
    public <T> void addRequestMapperForType(final Class<T> type,
                                            final Demapifier<T> requestMapper) {
        addRequestMapper((clazz, event) -> areOfType(type).test(clazz, event), requestMapper);
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

    @SuppressWarnings("unchecked")
    public <T> void addResponseSerializerForType(final Class<T> type,
                                                 final Mapifier<T> responseMapper) {
        addResponseSerializer(SerializationFilters.areOfType(type), (Mapifier<Object>) responseMapper);
    }

    public void addResponseSerializer(final Predicate<Object> filter,
                                      final Mapifier<Object> responseMapper) {
        validateNotNull(filter, "filter");
        validateNotNull(responseMapper, "responseMapper");
        serializers.add(serializationStage -> serializationStage
                .serializingResponseObjectsThat(filter)
                .using(responseMapper));
    }

    public void setSerializerAndDeserializer(final SerializerAndDeserializer serializerAndDeserializer) {
        this.serializerAndDeserializer = serializerAndDeserializer;
    }

    @Override
    public List<ChainModule> supplyModulesIfNotAlreadyPreset() {
        return singletonList(eventModule());
    }

    @Override
    public void init(final MetaData configurationMetaData) {
        final HandlerDistributors handlerDistributors = configurationMetaData.get(HANDLER_DISTRIBUTORS);
        handlerDistributors.register(handler -> handler instanceof Class, (handler, condition) -> {
            final Class<?> useCaseClass = (Class<?>) handler;
            final EventType eventType = eventTypeFromClass(useCaseClass);
            useCaseToEventMappings.put(useCaseClass, eventType);
            handlerDistributors.distribute(eventType, condition);
        });
    }

    @Override
    public void register(final ChainExtender extender) {
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
                        .deserializeObjectsPerDefault((targetType, map) ->
                                serializerAndDeserializer.deserialize(targetType, map));

        serializers.forEach(serializer -> serializer.accept(useCaseAdapterResponseSerializationStep1Builder));
        final UseCaseAdapter useCaseAdapter = useCaseAdapterResponseSerializationStep1Builder
                .serializingResponseObjectsOfTypeVoid().using(object -> emptyMap())
                .serializingObjectsByDefaultUsing(object -> serializerAndDeserializer.serialize(object))
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault()
                .buildAsStandaloneAdapter();

        final MessageBus messageBus = extender.getMetaDatum(MESSAGE_BUS);

        final SerializedMessageBus serializedMessageBus = useCaseAdapter.attachAndEnhance(messageBus);
        extender.addMetaDatum(SERIALIZED_MESSAGE_BUS, serializedMessageBus);
    }
}
