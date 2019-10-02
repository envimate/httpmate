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

package com.envimate.httpmate.websocketsusecases;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.generator.GenerationCondition;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.serializedMessageBus.SerializedMessageBus;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import com.envimate.messageMate.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static com.envimate.httpmate.usecases.UseCasesModule.SERIALIZED_MESSAGE_BUS;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.websocketsusecases.WebSocketMessageSenderConfiguration.webSocketMessageSenderConfiguration;
import static com.envimate.messageMate.subscribing.SubscriptionId.newUniqueId;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketSenderFactory {
    private final HttpMate httpMate;
    private final SerializedMessageBus serializedMessageBus;

    public static WebSocketSenderFactory webSocketSenderFactoryFor(final HttpMate httpMate) {
        validateNotNull(httpMate, "httpMate");
        final SerializedMessageBus serializedMessageBus = httpMate.getMetaDatum(SERIALIZED_MESSAGE_BUS);
        return new WebSocketSenderFactory(httpMate, serializedMessageBus);
    }

    public void createWebsocketSenderThatSendsToWebsocketsThat(final GenerationCondition condition,
                                                               final WebSocketMessageSender sender) {
        validateNotNull(condition, "condition");
        validateNotNull(sender, "sender");

        final WebSocketMessageSenderConfiguration configuration = webSocketMessageSenderConfiguration(serializedMessageBus);
        final EventType eventType = configuration.eventType();

        serializedMessageBus.subscribe(eventType, new Subscriber<>() {
            @Override
            public AcceptingBehavior accept(final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> message) {
                return null;
            }

            @Override
            public SubscriptionId getSubscriptionId() {
                return newUniqueId();
            }
        });

        sender.configure(configuration);
    }
}
