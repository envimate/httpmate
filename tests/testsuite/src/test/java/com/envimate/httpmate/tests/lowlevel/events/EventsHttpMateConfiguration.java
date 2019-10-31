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

package com.envimate.httpmate.tests.lowlevel.events;

import com.envimate.httpmate.HttpMate;
import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusType;

import java.util.HashMap;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.events.EventConfigurators.toUseTheMessageBus;
import static com.envimate.messageMate.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.processingContext.EventType.eventTypeFromString;

final class EventsHttpMateConfiguration {
    private static MessageBus messageBus;

    private EventsHttpMateConfiguration() {
    }

    static HttpMate theEventsHttpMateInstanceUsedForTesting() {
        messageBus = aMessageBus()
                .forType(MessageBusType.ASYNCHRONOUS)
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousConfiguration(4))
                .build();

        final HttpMate httpMate = anHttpMate()
                .get("/trigger", eventTypeFromString("trigger"))
                //.configured(toUseModules(eventModule()))
                .configured(toUseTheMessageBus(messageBus))
                .build();

        messageBus.subscribeRaw(eventTypeFromString("trigger"), processingContext -> {
            final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
            messageBus.send(eventTypeFromString("answer"), new HashMap<>(), correlationId);
        });

        return httpMate;
    }
}
