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

package com.envimate.httpmate.util.describing;

import java.util.Map;

import static com.envimate.httpmate.util.describing.CardBuilder.cardBuilder;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

public final class MapDumper {

    private MapDumper() {
    }

    public static String describe(final String title, final Map<String, Object> map) {
        final CardBuilder cardBuilder = cardBuilder();
        cardBuilder.setHeadline(title);
        map.forEach((key, value) -> {
            final String valueAsString = valueToString(value);
            cardBuilder.addEntry(key, valueAsString);
        });
        return cardBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private static String valueToString(final Object value) {
        if (isNull(value)) {
            return "null";
        }
        if (!(value instanceof Map)) {
            return value.toString();
        }
        final Map<Object, Object> map = (Map<Object, Object>) value;
        return map.entrySet().stream()
                .map(entry -> entry.getKey().toString() + " = " + entry.getValue().toString())
                .collect(joining("\n"));
    }
}
