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

package com.envimate.httpmate.client;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestPath {
    private final List<UriString> pathElements;
    private final List<QueryParameter> queryParameters;
    private final boolean trailingSlash;

    public static RequestPath parse(final String raw) {
        validateNotNull(raw, "raw");
        final String rawPath;
        final List<QueryParameter> queryParameters;
        if (!raw.contains("?")) {
            rawPath = raw;
            queryParameters = new LinkedList<>();
        } else {
            final int splitPosition = raw.indexOf('?');
            rawPath = raw.substring(0, splitPosition);
            final String queryString = raw.substring(splitPosition + 1);

            final String[] keyValues = queryString.split("&");
            queryParameters = Stream.of(keyValues)
                    .map(QueryParameter::parse)
                    .collect(toList());
        }
        final List<UriString> pathElements = Stream.of(rawPath.split("/"))
                .filter(string -> !string.isEmpty())
                .map(UriString::uriString)
                .collect(toList());
        final boolean trailingSlash = rawPath.endsWith("/");
        return new RequestPath(pathElements, queryParameters, trailingSlash);
    }

    public void add(final QueryParameter queryParameter) {
        validateNotNull(queryParameter, "queryParameter");
        queryParameters.add(queryParameter);
    }

    public String render() {
        final String path = path();

        final String queryString;
        if (queryParameters.isEmpty()) {
            queryString = "";
        } else {
            queryString = queryParameters.stream()
                    .map(QueryParameter::render)
                    .collect(joining("&", "?", ""));
        }
        return path + queryString;
    }

    public String path() {
        final String suffix;
        if(trailingSlash) {
            suffix = "/";
        } else {
            suffix = "";
        }
        return pathElements.stream()
                .map(UriString::encoded)
                .collect(joining("/", "/", suffix));
    }

    public List<QueryParameter> queryParameters() {
        return new ArrayList<>(queryParameters);
    }
}
