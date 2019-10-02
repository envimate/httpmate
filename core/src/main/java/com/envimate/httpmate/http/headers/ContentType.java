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

package com.envimate.httpmate.http.headers;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class ContentType {
    private static final String TEXT_PLAIN = "text/plain";
    private static final String JSON = "application/json";
    private static final String XML = "application/xml";
    private static final String YAML = "application/yaml";
    private static final String FORM_URLENCODED = "application/x-www-form-urlencoded";

    private final HeaderValueWithComment value;

    public static ContentType fromString(final Optional<String> value) {
        return value.map(raw -> {
            final HeaderValueWithComment header = HeaderValueWithComment.fromString(raw);
            return new ContentType(header);
        }).orElseGet(ContentType::empty);
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

    public static ContentType xml() {
        return fromString(XML);
    }

    public static ContentType yaml() {
        return fromString(YAML);
    }

    public static ContentType formUrlEncoded() {
        return fromString(FORM_URLENCODED);
    }

    public static ContentType textPlain() {
        return fromString(TEXT_PLAIN);
    }

    public boolean isEmpty() {
        return isNull(value);
    }

    public String comment() {
        return value.comment();
    }

    public String valueWithComment() {
        return value.valueWithComment();
    }

    public String internalValueForMapping() {
        return ofNullable(value)
                .map(HeaderValueWithComment::value)
                .orElse("");
    }
}
