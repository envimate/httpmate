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

package com.envimate.httpmate.client;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.StringJoiner;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BasePath {
    private final List<String> elements;

    static BasePath basePath(final String basePath) {
        validateNotNull(basePath, "basePath");
        final List<String> basePathElements = splitPath(basePath);
        return new BasePath(basePathElements);
    }

    public String concatenateWithStartingAndTrailingSlash(final String subPath) {
        final StringJoiner pathJoiner = new StringJoiner("/", "/", "/");
        elements.forEach(pathJoiner::add);
        final List<String> subElements = splitPath(subPath);
        subElements.forEach(pathJoiner::add);
        return pathJoiner.toString();
    }

    private static List<String> splitPath(final String path) {
        return stream(path.split("/"))
                .filter(element -> !element.isEmpty())
                .collect(toList());
    }
}
