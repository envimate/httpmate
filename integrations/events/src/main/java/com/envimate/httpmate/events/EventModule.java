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

package com.envimate.httpmate.events;

import com.envimate.httpmate.chains.*;
import com.envimate.httpmate.events.mapper.EventToResponseMapper;
import com.envimate.httpmate.events.mapper.RequestToEventMapper;
import com.envimate.httpmate.filtermap.FilterMapBuilder;
import com.envimate.httpmate.generator.GenerationCondition;
import com.envimate.httpmate.generator.Generator;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.processingContext.EventType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;
import java.util.function.Predicate;

import static com.envimate.httpmate.HttpMateChains.*;
import static com.envimate.httpmate.chains.ChainRegistry.CHAIN_REGISTRY;
import static com.envimate.httpmate.chains.MetaData.emptyMetaData;
import static com.envimate.httpmate.chains.MetaDataKey.metaDataKey;
import static com.envimate.httpmate.chains.rules.Drop.drop;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.events.EventsChains.*;
import static com.envimate.httpmate.events.mapper.RequestToEventMapper.byDirectlyMappingAllData;
import static com.envimate.httpmate.events.processors.DetermineEventProcessor.determineEventProcessor;
import static com.envimate.httpmate.events.processors.DispatchEventProcessor.dispatchEventProcessor;
import static com.envimate.httpmate.events.processors.HandleExternalEventProcessor.handleExternalEventProcessor;
import static com.envimate.httpmate.events.processors.MapToEventProcessor.mapToEventProcessor;
import static com.envimate.httpmate.events.processors.SerializationProcessor.serializationProcessor;
import static com.envimate.httpmate.events.processors.UnwrapDispatchingExceptionProcessor.unwrapDispatchingExceptionProcessor;
import static com.envimate.httpmate.filtermap.FilterMapBuilder.filterMapBuilder;
import static com.envimate.httpmate.generator.Generator.generator;
import static com.envimate.httpmate.generator.Generators.generators;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventModule implements ChainModule {
    public static final MetaDataKey<MessageBus> MESSAGE_BUS = metaDataKey("MESSAGE_BUS");
    public static final MetaDataKey<Boolean> IS_EXTERNAL_EVENT = metaDataKey("IS_EXTERNAL_EVENT");
    public static final MetaDataKey<EventType> EVENT_TYPE = metaDataKey("EVENT_TYPE");
    public static final MetaDataKey<Map<String, Object>> EVENT = metaDataKey("EVENT");
    public static final MetaDataKey<Optional<Map<String, Object>>> EVENT_RETURN_VALUE = metaDataKey("EVENT_RETURN_VALUE");

    private MessageBus messageBus;
    private final List<Generator<EventType>> eventTypeGenerators = new LinkedList<>();
    private final FilterMapBuilder<MetaData, RequestToEventMapper> requestToEventMappers = filterMapBuilder();
    private EventToResponseMapper responseMapper;
    private final Map<EventType, ExternalEventMapping> externalEventMappings = new HashMap<>();

    public static EventModule eventModule() {
        final EventModule eventModule = new EventModule();
        eventModule.setDefaultRequestToEventMapper(byDirectlyMappingAllData());
        return eventModule;
    }

    public void setMessageBus(final MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    public void addRequestToEventMapper(final Predicate<MetaData> filter,
                                        final RequestToEventMapper mapper) {
        validateNotNull(filter, "filter");
        validateNotNull(mapper, "mapper");
        requestToEventMappers.put(filter, mapper);
    }

    public void setResponseMapper(final EventToResponseMapper responseMapper) {
        validateNotNull(responseMapper, "responseMapper");
        this.responseMapper = responseMapper;
    }

    public void setDefaultRequestToEventMapper(final RequestToEventMapper requestToEventMapper) {
        validateNotNull(requestToEventMapper, "requestToEventMapper");
        requestToEventMappers.setDefaultValue(requestToEventMapper);
    }

    public void addEventMapping(final EventType eventType,
                                final GenerationCondition condition) {
        validateNotNull(eventType, "eventType");
        validateNotNull(condition, "condition");
        final Generator<EventType> eventTypeGenerator = generator(eventType, condition);
        eventTypeGenerators.add(eventTypeGenerator);
    }

    public void addExternalEventMapping(final EventType eventType,
                                        final ExternalEventMapping externalEventMapping) {
        validateNotNull(eventType, "eventType");
        validateNotNull(externalEventMapping, "externalEventMapping");
        externalEventMappings.put(eventType, externalEventMapping);
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        dependencyRegistry.setMetaDatum(MESSAGE_BUS, messageBus);
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.addProcessor(DETERMINE_HANDLER, determineEventProcessor(generators(eventTypeGenerators)));

        extender.routeIfSet(PREPARE_RESPONSE, jumpTo(MAP_REQUEST_TO_EVENT), EVENT_TYPE);

        extender.createChain(MAP_REQUEST_TO_EVENT, jumpTo(SUBMIT_EVENT), jumpTo(EXCEPTION_OCCURRED));
        extender.addProcessor(MAP_REQUEST_TO_EVENT, mapToEventProcessor(requestToEventMappers.build()));

        extender.createChain(SUBMIT_EVENT, jumpTo(MAP_EVENT_TO_RESPONSE), jumpTo(EXCEPTION_OCCURRED));
        extender.addProcessor(SUBMIT_EVENT, dispatchEventProcessor(messageBus));

        extender.createChain(MAP_EVENT_TO_RESPONSE, jumpTo(POST_PROCESS), jumpTo(EXCEPTION_OCCURRED));
        extender.addProcessor(MAP_EVENT_TO_RESPONSE, serializationProcessor(responseMapper));

        extender.addProcessor(PREPARE_EXCEPTION_RESPONSE, unwrapDispatchingExceptionProcessor());

        extender.createChain(EXTERNAL_EVENT, drop(), jumpTo(EXCEPTION_OCCURRED));
        extender.addProcessor(EXTERNAL_EVENT, handleExternalEventProcessor(externalEventMappings));

        extender.routeIfFlagIsSet(INIT, jumpTo(EXTERNAL_EVENT), IS_EXTERNAL_EVENT);

        externalEventMappings.forEach((eventType, externalEventMapping) ->
                externalEventMapping.jumpTarget().ifPresent(chainName ->
                        extender.routeIfEquals(EXTERNAL_EVENT, jumpTo(chainName), EVENT_TYPE, eventType)));

        final ChainRegistry chainRegistry = extender.getMetaDatum(CHAIN_REGISTRY);
        registerEventHandlers(messageBus, chainRegistry);
    }

    @SuppressWarnings("unchecked")
    private void registerEventHandlers(final MessageBus messageBus,
                                       final ChainRegistry chainRegistry) {
        externalEventMappings.forEach((type, mapping) -> messageBus.subscribe(type, event -> {
            final MetaData metaData = emptyMetaData();
            metaData.set(EVENT_RETURN_VALUE, of((Map<String, Object>) event));
            metaData.set(EVENT_TYPE, type);
            metaData.set(IS_EXTERNAL_EVENT, true);
            chainRegistry.putIntoChain(INIT, metaData, m -> {
            });
        }));
    }
}
