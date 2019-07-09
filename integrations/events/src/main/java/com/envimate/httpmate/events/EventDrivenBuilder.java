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

import com.envimate.httpmate.CoreModule;
import com.envimate.httpmate.HttpMateBuilder;
import com.envimate.httpmate.HttpMateConfigurationType;
import com.envimate.httpmate.events.builder.*;
import com.envimate.httpmate.events.mapper.EventToResponseMapper;
import com.envimate.httpmate.generator.builder.ConditionStage;
import com.envimate.messageMate.processingContext.EventType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.envimate.httpmate.CoreModule.coreModule;
import static com.envimate.httpmate.HttpMateBuilder.httpMateBuilder;
import static com.envimate.httpmate.events.EventModule.eventModule;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventDrivenBuilder {
    public static final HttpMateConfigurationType<Attached<EventStage<Stage1>>> EVENT_DRIVEN =
            EventDrivenBuilder::aHttpMateDispatchingEventsUsing;

    private final CoreModule coreModule;
    private final EventModule eventModule;

    public static Attached<EventStage<Stage1>> aHttpMateDispatchingEventsUsing() {
        final CoreModule coreModule = coreModule();
        final EventModule eventModule = eventModule();
        return messageBus -> {
            eventModule.setMessageBus(messageBus);
            final EventDrivenBuilder eventDrivenBuilder = new EventDrivenBuilder(coreModule, eventModule);
            return eventDrivenBuilder.new Stage1();
        };
    }

    public final class Stage1 implements EventStage<Stage1>, MapToResponseStage<HttpMateBuilder> {
        @Override
        public ConditionStage<Stage1> triggeringTheEvent(final EventType eventType) {
            return condition -> {
                eventModule.addEventMapping(eventType, condition);
                return this;
            };
        }

        @Override
        public By<Stage1> handlingTheEvent(final EventType eventType) {
            return mapping -> {
                eventModule.addExternalEventMapping(eventType, mapping);
                return this;
            };
        }

        @Override
        public HttpMateBuilder mappingResponsesUsing(final EventToResponseMapper mapper) {
            eventModule.setDefaultEventToResponseMapper(mapper);
            return httpMateBuilder(coreModule, eventModule);
        }
    }
}
