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

import com.envimate.httpmate.CoreModule;
import com.envimate.httpmate.HttpMateBuilder;
import com.envimate.httpmate.HttpMateConfigurationType;
import com.envimate.httpmate.events.EventModule;
import com.envimate.httpmate.events.builder.Using;
import com.envimate.httpmate.generator.GenerationCondition;
import com.envimate.httpmate.usecases.builder.SerializationAndDeserializationStage;
import com.envimate.httpmate.usecases.builder.UseCaseStage1;
import com.envimate.httpmate.usecases.builder.UseCaseStage2;
import com.envimate.httpmate.usecases.usecase.SerializerAndDeserializer;
import com.envimate.messageMate.mapping.Demapifier;
import com.envimate.messageMate.mapping.Mapifier;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.processingContext.EventType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Predicate;

import static com.envimate.httpmate.CoreModule.coreModule;
import static com.envimate.httpmate.HttpMateBuilder.httpMateBuilder;
import static com.envimate.httpmate.events.EventModule.eventModule;
import static com.envimate.httpmate.generator.PathAndMethodGenerationCondition.pathAndMethodEventTypeGenerationCondition;
import static com.envimate.httpmate.path.PathTemplate.pathTemplate;
import static com.envimate.httpmate.usecases.UseCasesModule.useCasesModule;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.processingContext.EventType.eventTypeFromString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UseCaseDrivenBuilder {
    public static final HttpMateConfigurationType<UseCaseStage1<Stage1>> USE_CASE_DRIVEN =
            UseCaseDrivenBuilder::useCaseDrivenBuilder;
    private static final int DEFAULT_POOL_SIZE = 4;

    private final CoreModule coreModule = coreModule();
    private final EventModule eventModule = eventModule();
    private final UseCasesModule useCasesModule = useCasesModule();

    public static UseCaseStage1<Stage1> useCaseDrivenBuilder() {
        return new UseCaseDrivenBuilder().new Stage1();
    }

    public final class Stage1 implements UseCaseStage1<Stage1>, SerializationAndDeserializationStage<HttpMateBuilder> {

        @Override
        public UseCaseStage2<Stage1> servingTheUseCase(final Class<?> useCaseClass) {
            return pathTemplate -> requestMethods -> {
                final EventType eventType = eventTypeFromString(useCaseClass.getName());
                final GenerationCondition eventTypeGenerationCondition =
                        pathAndMethodEventTypeGenerationCondition(pathTemplate(pathTemplate), requestMethods);
                eventModule.addEventMapping(eventType, eventTypeGenerationCondition);
                useCasesModule.addUseCaseToEventMapping(useCaseClass, eventType);
                return this;
            };
        }

        @Override
        public Using<SerializationAndDeserializationStage<HttpMateBuilder>, Mapifier<Object>>
        serializingResponseObjectsThat(final Predicate<Object> filter) {
            return mapper -> {
                useCasesModule.addResponseSerializer(filter, mapper);
                return this;
            };
        }

        @Override
        public <X> Using<SerializationAndDeserializationStage<HttpMateBuilder>, Demapifier<X>>
        mappingUseCaseParametersThat(final EventFilter<?> filter) {
            return mapper -> {
                useCasesModule.addRequestMapper(filter, mapper);
                return this;
            };
        }

        @Override
        public HttpMateBuilder mappingRequestsAndResponsesUsing(final SerializerAndDeserializer serializerAndDeserializer) {
            validateNotNull(serializerAndDeserializer, "serializerAndDeserializer");
            useCasesModule.setSerializerAndDeserializer(serializerAndDeserializer);

            final MessageBus messageBus = aMessageBus().forType(ASYNCHRONOUS)
                    .withAsynchronousConfiguration(
                            constantPoolSizeAsynchronousPipeConfiguration(DEFAULT_POOL_SIZE))
                    .build();
            eventModule.setMessageBus(messageBus);

            final HttpMateBuilder httpMateBuilder = httpMateBuilder(coreModule, eventModule, useCasesModule);
            httpMateBuilder.configured(serializerAndDeserializer);
            return httpMateBuilder;
        }
    }
}
