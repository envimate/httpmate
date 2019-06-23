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

package com.envimate.httpmate.path.statemachine;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MatchingResult {
    private final boolean successful;
    private final Map<String, String> captures;

    public static MatchingResult matchingResult(final boolean successful) {
        return new MatchingResult(successful, new HashMap<>());
    }

    public MatchingResult merge(final Map<String, String> otherCaptures) {
        final Map<String, String> mergedCaptures = new HashMap<>(captures);
        mergedCaptures.putAll(otherCaptures);
        return new MatchingResult(true, mergedCaptures);
    }

    public static MatchingResult success(final Map<String, String> captures) {
        return new MatchingResult(true, captures);
    }

    public static MatchingResult fail() {
        return new MatchingResult(false, new HashMap<>());
    }

    public boolean isSuccessful() {
        return successful;
    }

    public Map<String, String> captures() {
        return captures;
    }
}
