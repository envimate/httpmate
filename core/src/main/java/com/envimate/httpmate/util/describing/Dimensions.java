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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.Math.min;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class Dimensions {
    private static final int MAX_WIDTH = 120;
    private static final int SLACK = 6;
    private final int width;
    private final int firstRowLength;
    private final int lastRowLength;

    static Dimensions calculateDimensions(final Map<String, EscapedText> map) {
        validateNotNull(map, "map");
        final int firstRowLength = keyLength(map);
        final int lastRowLength = valueLength(map);
        final int width = firstRowLength + lastRowLength + SLACK;
        return new Dimensions(width, firstRowLength, lastRowLength);
    }

    private static int keyLength(final Map<String, EscapedText> map) {
        return map.keySet().stream()
                .mapToInt(String::length)
                .max()
                .getAsInt();
    }

    private static int valueLength(final Map<String, EscapedText> map) {
        final int maxLineLength = map.values().stream()
                .mapToInt(EscapedText::maxLineLength)
                .max().getAsInt();
        return min(maxLineLength, MAX_WIDTH);
    }

    int width() {
        return width;
    }

    int firstRowLength() {
        return firstRowLength;
    }

    int lastRowLength() {
        return lastRowLength;
    }
}
