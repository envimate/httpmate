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

package com.envimate.httpmate.request;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class ContentType {
    private static final String JSON = "application/json";

    private final String value;

    public static ContentType fromString(final Optional<String> value) {
        return value
                .map(String::toLowerCase)
                .map(ContentType::new)
                .orElseGet(ContentType::empty);
    }

    private static ContentType empty() {
        return new ContentType(null);
    }

    public static ContentType fromString(final String contentType) {
        validateNotNull(contentType, "content-type");
        return fromString(Optional.of(contentType));
    }

    public static ContentType json() {
        return fromString(JSON);
    }

    public boolean isEmpty() {
        return isNull(value);
    }

    public boolean startsWith(final ContentType other) {
        if (value == null) {
            return false;
        }
        if (other.value == null) {
            return false;
        }
        return value.startsWith(other.value);
    }

    public String internalValueForMapping() {
        return value;
    }
}
