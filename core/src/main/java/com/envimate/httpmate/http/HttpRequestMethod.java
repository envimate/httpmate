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

package com.envimate.httpmate.http;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpRequestMethod {
    public static final HttpRequestMethod GET = parse("GET");
    public static final HttpRequestMethod POST = parse("POST");
    public static final HttpRequestMethod PUT = parse("PUT");
    public static final HttpRequestMethod DELETE = parse("DELETE");
    public static final HttpRequestMethod OPTIONS = parse("OPTIONS");
    public static final HttpRequestMethod HEAD = parse("HEAD");
    public static final HttpRequestMethod CONNECT = parse("CONNECT");
    public static final HttpRequestMethod TRACE = parse("TRACE");
    public static final HttpRequestMethod PATCH = parse("PATCH");

    private final String value;

    public static HttpRequestMethod parse(final String requestMethod) {
        validateNotNullNorEmpty(requestMethod, "requestMethod");
        return new HttpRequestMethod(requestMethod);
    }

    public String name() {
        return value;
    }
}
