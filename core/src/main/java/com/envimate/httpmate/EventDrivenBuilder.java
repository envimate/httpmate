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
import com.envimate.messageMate.messageBus.MessageBus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.envimate.httpmate.HttpMateConfigurator.httpMateConfigurator;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventDrivenBuilder {
    private final HttpMateConfigurator builder = httpMateConfigurator();
    private final MessageBus messageBus;

    static EventStage1<Stage1> eventDrivenBuilder(final MessageBus messageBus) {
        return new EventDrivenBuilder(messageBus).new Stage1();
    }

    public final class Stage1 implements EventStage1<Stage1>, MapToEventStage<Stage2> {
        @Override
        public EventStage2<Stage1> choosingTheEvent(final EventType eventType) {
            return pathTemplate -> requestMethods -> {
                builder.addEventMapping(eventType, pathTemplate, requestMethods);
                return this;
            };
        }

        @Override
        public Using<MapToEventStage<Stage2>, RequestToEventMapper> preparingRequestsForParameterMappingThat(
                final Predicate<MetaData> filter) {
            return requestToEventMapper -> {
                builder.addRequestToEventMapper(filter, requestToEventMapper);
                return this;
            };
        }

        @Override
        public Stage2 preparingRequestsForParameterMapping(final RequestToEventMapper mapper) {
            builder.setDefaultRequestToEventMapper(mapper);
            return new Stage2();
        }
    }

    public final class Stage2 implements MapToResponseStage<Stage3> {
        @Override
        public Stage3 mappingResponsesUsing(final EventToResponseMapper mapper) {
            builder.setResponseMapper(mapper);
            return new Stage3();
        }
    }

    public final class Stage3 {
        public HttpMate configuredBy(final Consumer<HttpMateConfigurator> configurator) {
            configurator.accept(builder);
            return builder.build(messageBus);
        }
    }
}
