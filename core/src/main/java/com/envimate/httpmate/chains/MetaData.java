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

package com.envimate.httpmate.chains;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetaData {
    private final Map<String, Object> map;

    static MetaData metaData(final Map<String, Object> map) {
        validateNotNull(map, "map");
        return new MetaData(map);
    }

    public static MetaData emptyMetaData() {
        return new MetaData(new HashMap<>());
    }

    public <T> void set(final MetaDataKey<T> key, final T value) {
        validateNotNull(key, "key");
        map.put(key.key(), value);
    }

    public <T> T get(final MetaDataKey<T> key) {
        return getOptional(key).orElseThrow(() -> new RuntimeException(format(
                "Could not find meta datum %s in %s", key.key(), map)));
    }

    public <T> T getAs(final MetaDataKey<? super T> key, final Class<T> type) {
        return (T) get(key);
    }

    public <T> Optional<T> getOptional(final MetaDataKey<T> key) {
        final T datum = (T) map.get(key.key());
        return ofNullable(datum);
    }

    public <T> Optional<T> getOptionalAs(final MetaDataKey<? super T> key, final Class<T> type) {
        return (Optional<T>) getOptional(key);
    }

    public boolean contains(final MetaDataKey<?> key) {
        return getOptional(key).isPresent();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
