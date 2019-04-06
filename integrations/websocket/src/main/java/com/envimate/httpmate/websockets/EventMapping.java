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

package com.envimate.httpmate.websockets;

import com.envimate.httpmate.chains.MetaData;
import com.envimate.messageMate.messageBus.EventType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;
import java.util.function.Predicate;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMapping {
    private final EventType eventType;
    private final Predicate<MetaData> predicate;

    static EventMapping eventMapping(final EventType eventType, final Predicate<MetaData> predicate) {
        validateNotNull(eventType, "eventType");
        validateNotNull(predicate, "predicate");
        return new EventMapping(eventType, predicate);
    }

    public Optional<EventType> getEventIfMatching(final MetaData metaData) {
        if (predicate.test(metaData)) {
            return of(eventType);
        } else {
            return empty();
        }
    }
}
