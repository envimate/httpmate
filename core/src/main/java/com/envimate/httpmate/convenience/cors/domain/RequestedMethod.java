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

package com.envimate.httpmate.convenience.cors.domain;

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.convenience.cors.Cors;
import com.envimate.httpmate.http.HttpRequestMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

import static com.envimate.httpmate.HttpMateChainKeys.HEADERS;
import static com.envimate.httpmate.http.HttpRequestMethod.*;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.util.Arrays.asList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestedMethod {
    private static final List<HttpRequestMethod> SIMPLE_METHODS = asList(GET, POST, HEAD);
    private final HttpRequestMethod method;

    public static RequestedMethod load(final MetaData metaData) {
        validateNotNull(metaData, "metaData");
        final String method = metaData.get(HEADERS).getHeader(Cors.ACCESS_CONTROL_REQUEST_METHOD).orElseThrow();
        validateNotNullNorEmpty(method, "method");
        return new RequestedMethod(parse(method));
    }

    public boolean isSimpleMethod() {
        return SIMPLE_METHODS.contains(method);
    }

    public String internalValueForMapping() {
        return method.name();
    }

    public boolean matches(final HttpRequestMethod method) {
        validateNotNull(method, "method");
        return Objects.equals(method, this.method);
    }
}
