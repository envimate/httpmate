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

package com.envimate.httpmate.chains;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class IndexedModules {
    private final List<ChainModule> modules;

    static IndexedModules indexedModules(final List<ChainModule> initialModules) {
        validateNotNull(initialModules, "initialModules");
        return new IndexedModules(new LinkedList<>(initialModules));
    }

    <T extends ChainModule> T get(final Class<T> type) {
        return find(type).orElseThrow(() -> new RuntimeException(format("Module of type '%s' not found", type.getSimpleName())));
    }

    public boolean contains(final Class<? extends ChainModule> type) {
        return find(type).isPresent();
    }

    public void add(final ChainModule module) {
        validateNotNull(module, "module");
        modules.add(module);
    }

    public Stream<? extends ChainModule> stream() {
        return modules.stream();
    }

    @SuppressWarnings("unchecked")
    private <T extends ChainModule> Optional<T> find(final Class<T> type) {
        validateNotNull(type, "type");
        for (final ChainModule module : modules) {
            if (type.isInstance(module)) {
                return of((T) module);
            }
        }
        return empty();
    }
}
