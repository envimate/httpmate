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

package com.envimate.httpmate.convenience.event;

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.event.EventTypeGenerationCondition;
import com.envimate.httpmate.path.PathTemplate;
import com.envimate.httpmate.request.HttpRequestMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

import static com.envimate.httpmate.chains.HttpMateChainKeys.*;
import static com.envimate.httpmate.request.HttpRequestMethod.*;
import static com.envimate.httpmate.request.PathParameters.pathParameters;
import static com.envimate.httpmate.util.Validators.validateArrayNeitherNullNorEmptyNorContainsNull;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathAndMethodEventTypeGenerationCondition implements EventTypeGenerationCondition {
    private static final List<HttpRequestMethod> METHOD_WHITELIST = asList(GET, POST, PUT, DELETE);

    private final PathTemplate pathTemplate;
    private final HttpRequestMethod[] methods;

    public static PathAndMethodEventTypeGenerationCondition pathAndMethodEventTypeGenerationCondition(
            final PathTemplate pathTemplate,
            final HttpRequestMethod[] methods) {
        validateNotNull(pathTemplate, "pathTemplate");
        validateArrayNeitherNullNorEmptyNorContainsNull(methods, "methods");
        stream(methods).forEach(method -> {
            if (!METHOD_WHITELIST.contains(method)) {
                throw new IllegalArgumentException(format("Use cases cannot be registered on the %s method.", method.name()));
            }
        });
        return new PathAndMethodEventTypeGenerationCondition(pathTemplate, methods);
    }

    @Override
    public boolean generate(final MetaData metaData) {
        final HttpRequestMethod method = metaData.get(METHOD);
        if (stream(methods).noneMatch(method::equals)) {
            return false;
        }
        final String path = metaData.get(PATH);
        if (!pathTemplate.matches(path)) {
            return false;
        }

        final Map<String, String> pathParameters = pathTemplate.extractPathParameters(path);
        metaData.set(PATH_PARAMETERS, pathParameters(pathParameters));
        return true;
    }
}
