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

import com.envimate.httpmate.builder.*;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.mapper.EventToResponseMapper;
import com.envimate.httpmate.mapper.RequestToEventMapper;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestMapper;
import com.envimate.messageMate.useCaseAdapter.mapping.ResponseMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.envimate.httpmate.HttpMateConfigurator.httpMateConfigurator;
import static com.envimate.httpmate.UseCaseConfigurator.useCaseConfigurator;
import static com.envimate.messageMate.messageBus.EventType.eventTypeFromString;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UseCaseDrivenBuilder {
    private final HttpMateConfigurator builder;
    private final UseCaseConfigurator useCaseConfigurator = useCaseConfigurator();

    static UseCaseStage1<Stage1> useCaseDrivenBuilder() {
        final HttpMateConfigurator builder = httpMateConfigurator();
        return new UseCaseDrivenBuilder(builder).new Stage1();
    }

    public final class Stage1 implements UseCaseStage1<Stage1>, DeserializationStage<Stage2> {

        @Override
        public UseCaseStage2<Stage1> servingTheUseCase(final Class<?> useCaseClass) {
            return pathTemplate -> requestMethods -> {
                final EventType eventType = eventTypeFromString(useCaseClass.getName());
                useCaseConfigurator.addUseCaseToEventMapping(useCaseClass, eventType);
                builder.addEventMapping(eventType, pathTemplate, requestMethods);
                return this;
            };
        }

        @Override
        public <X> Using<DeserializationStage<Stage2>, RequestMapper<X>> mappingRequestsToUseCaseParametersThat(
                final BiPredicate<Class<?>, Map<String, Object>> filter) {
            return mapper -> {
                useCaseConfigurator.addRequestMapper(filter, mapper);
                return this;
            };
        }

        @Override
        public Stage2 mappingRequestsToUseCaseParametersByDefaultUsing(final RequestMapper<Object> mapper) {
            useCaseConfigurator.setDefaultRequestMapper(mapper);
            return new Stage2();
        }
    }

    public final class Stage2 implements MapToEventStage<Stage5> {

        @Override
        public Using<MapToEventStage<Stage5>, RequestToEventMapper> mappingRequestsToEventsThat(
                final Predicate<MetaData> filter) {
            return mapper -> {
                builder.addRequestToEventMapper(filter, mapper);
                return this;
            };
        }

        @Override
        public Stage5 mappingRequestsToEventsUsing(final RequestToEventMapper mapper) {
            builder.setDefaultRequestToEventMapper(mapper);
            return new Stage5();
        }
    }

    public final class Stage5 implements SerializationStage<Stage6> {

        @Override
        public Using<SerializationStage<Stage6>, ResponseMapper<Object>> serializingResponseObjectsThat(
                final Predicate<Object> filter) {
            return mapper -> {
                useCaseConfigurator.addResponseSerializer(filter, mapper);
                return this;
            };
        }

        @Override
        public Stage6 serializingResponseObjectsByDefaultUsing(final ResponseMapper<Object> mapper) {
            useCaseConfigurator.setDefaultResponseSerializer(mapper);
            return new Stage6();
        }
    }

    public final class Stage6 implements MapToResponseStage<Stage7> {

        @Override
        public Stage7 mappingEventsToResponsesUsing(final EventToResponseMapper mapper) {
            builder.setResponseMapper(mapper);
            return new Stage7();
        }
    }

    public final class Stage7 {
        public HttpMate configuredBy(final BiConsumer<HttpMateConfigurator, UseCaseConfigurator> configurator) {
            configurator.accept(builder, useCaseConfigurator);
            return useCaseConfigurator.build(builder);
        }
    }
}
