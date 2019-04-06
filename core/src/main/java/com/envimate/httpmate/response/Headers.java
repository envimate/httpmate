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

package com.envimate.httpmate.response;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static com.envimate.httpmate.convenience.Http.Headers.CONTENT_LENGTH;
import static com.envimate.httpmate.response.HeaderKey.headerKey;
import static com.envimate.httpmate.response.ResponseValidationException.responseValidationException;
import static com.envimate.httpmate.util.Maps.valueObjectsToStrings;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class Headers {
    private final Map<HeaderKey, HeaderValue> headers;

    static Headers headers(final Map<HeaderKey, HeaderValue> headers) {
        validateNotNull(headers, "headers");
        if(headers.containsKey(headerKey(CONTENT_LENGTH))) {
            throw responseValidationException("Content-Length must not be set in responses.");
        }
        return new Headers(headers);
    }

    Map<String, String> asStringMap() {
        return valueObjectsToStrings(headers, HeaderKey::stringValue, HeaderValue::stringValue);
    }
}
