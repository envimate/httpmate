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

package com.envimate.httpmate.generator;

import com.envimate.httpmate.chains.MetaData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Generator<T> {
    private final T value;
    private final GenerationCondition condition;

    public static <T> Generator<T> generator(final T value, final GenerationCondition condition) {
        validateNotNull(value, "value");
        validateNotNull(condition, "condition");
        return new Generator<>(value, condition);
    }

    boolean isSubsetOf(final Generator<T> other) {
        validateNotNull(other, "other");
        return condition.isSubsetOf(other.condition);
    }

    Optional<T> generate(final MetaData metaData) {
        if(condition.generate(metaData)) {
            return of(value);
        }
        return empty();
    }
}
