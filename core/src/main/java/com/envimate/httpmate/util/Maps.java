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

package com.envimate.httpmate.util;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;

public final class Maps {

    private Maps() {
    }

    public static <K, V> Optional<V> getOptionally(final Map<K, V> map, final K key) {
        if (!map.containsKey(key)) {
            return empty();
        }
        return of(map.get(key));
    }

    public static <K, V> Map<K, V> stringsToValueObjects(final Map<String, String> map,
                                                         final Function<String, K> keyMapper,
                                                         final Function<String, V> valueMapper) {
        return transformMap(map, keyMapper, valueMapper);

    }

    public static <K, V> Map<String, String> valueObjectsToStrings(final Map<K, V> map,
                                                                   final Function<K, String> keyMapper,
                                                                   final Function<V, String> valueMapper) {
        return transformMap(map, keyMapper, valueMapper);
    }

    private static <A, B, Y, Z> Map<Y, Z> transformMap(final Map<A, B> map,
                                                       final Function<A, Y> keyMapper,
                                                       final Function<B, Z> valueMapper) {
        return map.entrySet().stream()
                .collect(toMap(
                        entry -> keyMapper.apply(entry.getKey()),
                        entry -> valueMapper.apply(entry.getValue())));
    }
}
