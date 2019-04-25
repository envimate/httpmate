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

package com.envimate.httpmate.chains.graph;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.envimate.httpmate.chains.graph.Color.*;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Arrays.asList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColorPool<T> {
    private final List<Color> remainingColors;
    private final Map<T, Color> assignedColors;

    public static <T> ColorPool<T> colorPool() {
        final List<Color> availableColors = asList(BLACK, ORANGE, GREEN, BLUE, VIOLET, PURPLE, YELLOW);
        return new ColorPool<>(new LinkedList<>(availableColors), new HashMap<>());
    }

    public Color assign(final T key) {
        validateNotNull(key, "key");
        if (assignedColors.containsKey(key)) {
            return assignedColors.get(key);
        }

        final Color nextColor = remainingColors.get(0);
        remainingColors.remove(0);
        assignedColors.put(key, nextColor);
        return nextColor;
    }
}
