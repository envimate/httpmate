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

import java.util.function.Function;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UriString {
    private final String value;

    public static UriString uriString(final String value) {
        validateNotNull(value, "value");
        return new UriString(value);
    }

    public String encoded(final Function<String, String> encoder) {
        return encoder.apply(value);
    }

    public String encoded() {
        return encoded(UriString::urlEncode);
    }

    public String unencoded() {
        return value;
    }

    private static String urlEncode(final String string) {
        return encode(string, UTF_8);
    }
}
