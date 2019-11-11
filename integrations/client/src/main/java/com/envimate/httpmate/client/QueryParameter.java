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

import com.envimate.httpmate.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static com.envimate.httpmate.client.UriString.uriString;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParameter {
    private final UriString key;
    private final UriString value;

    public static QueryParameter queryParameter(final String key, final String value) {
        validateNotNullNorEmpty(key, "key");
        validateNotNull(value, "value");
        return new QueryParameter(uriString(key), uriString(value));
    }

    public static QueryParameter queryParameter(final String key) {
        validateNotNullNorEmpty(key, "key");
        return new QueryParameter(uriString(key), null);
    }

    public static QueryParameter parse(final String keyValue) {
        Validators.validateNotNull(keyValue, "keyValue");
        if (!keyValue.contains("=")) {
            return queryParameter(keyValue);
        }

        final int splitPosition = keyValue.indexOf('=');
        final String key = keyValue.substring(0, splitPosition);
        final String value = keyValue.substring(splitPosition + 1);
        return queryParameter(key, value);
    }

    public String render() {
        if(isNull(value)) {
            return key.encoded();
        }
        return format("%s=%s", key.encoded(), value.encoded());
    }

    public UriString key() {
        return key;
    }

    public Optional<UriString> value() {
        return ofNullable(value);
    }
}
