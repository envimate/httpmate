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

package com.envimate.httpmate.filtermap;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static com.envimate.httpmate.filtermap.FilterMap.filterMap;
import static com.envimate.httpmate.filtermap.FilterMapEntry.filterMapEntry;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilterMapBuilder<F, T> {
    private final List<FilterMapEntry<F, T>> entries;
    private T defaultValue;

    public static <F, T> FilterMapBuilder<F, T> filterMapBuilder() {
        return new FilterMapBuilder<>(new LinkedList<>());
    }

    public void put(final Predicate<F> filter, final T value) {
        final FilterMapEntry<F, T> entry = filterMapEntry(filter, value);
        entries.add(entry);
    }

    public void setDefaultValue(final T defaultValue) {
        validateNotNull(defaultValue, "defaultValue");
        this.defaultValue = defaultValue;
    }

    public FilterMap<F, T> build() {
        return filterMap(entries, defaultValue);
    }
}
