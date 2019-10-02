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

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.serializedMessageBus.SerializedMessageBus;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static java.lang.String.format;
import static java.util.Objects.isNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketMessageSender {
    private volatile WebSocketMessageSenderConfiguration configuration;

    public static WebSocketMessageSender webSocketMessageSender() {
        return new WebSocketMessageSender();
    }

    void configure(final WebSocketMessageSenderConfiguration configuration) {
        this.configuration = configuration;
    }

    public void send(final Object message) {
        if (isNull(configuration)) {
            throw new UnsupportedOperationException(format(
                    "%s has not been registered with a HttpMate instance", WebSocketMessageSender.class.getName()));
        }
        final EventType eventType = configuration.eventType();
        final SerializedMessageBus serializedMessageBus = configuration.serializedMessageBus();
        serializedMessageBus.serializeAndSend(eventType, message);
    }
}
