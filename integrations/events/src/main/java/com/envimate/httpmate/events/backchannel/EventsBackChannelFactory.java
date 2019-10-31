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

package com.envimate.httpmate.events.backchannel;

import com.envimate.httpmate.backchannel.BackChannelFactory;
import com.envimate.httpmate.backchannel.BackChannelTrigger;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.messageMate.processingContext.EventType.eventTypeFromString;
import static com.envimate.messageMate.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;
import static com.envimate.messageMate.subscribing.SubscriptionId.newUniqueId;
import static java.util.UUID.randomUUID;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventsBackChannelFactory implements BackChannelFactory {
    private final MessageBus messageBus;

    @Override
    public BackChannelTrigger createTrigger(final Runnable action) {
        validateNotNull(action, "action");

        final EventType eventType = randomEventType();

        messageBus.subscribe(eventType, new Subscriber<>() {
            @Override
            public AcceptingBehavior accept(final Object message) {
                action.run();
                return MESSAGE_ACCEPTED;
            }

            @Override
            public SubscriptionId getSubscriptionId() {
                return newUniqueId();
            }
        });

        return () -> messageBus.send(eventType, new Object());
    }

    private static EventType randomEventType() {
        final String uuid = randomUUID().toString();
        return eventTypeFromString(uuid);
    }
}
