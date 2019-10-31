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

package com.envimate.httpmate.http.headers.cookies;

import com.envimate.httpmate.http.Headers;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.envimate.httpmate.http.Http.Headers.COOKIE;
import static com.envimate.httpmate.http.headers.cookies.CookieName.cookieName;
import static com.envimate.httpmate.http.headers.cookies.CookieValue.cookieValue;
import static com.envimate.httpmate.util.Maps.getOptionally;
import static java.lang.String.format;
import static java.util.Arrays.stream;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Cookies {
    private final Map<CookieName, CookieValue> cookies;

    public static Cookies cookiesFromHeaders(final Headers headers) {
        final Map<CookieName, CookieValue> cookies = new HashMap<>();
        headers.getOptionalHeader(COOKIE).ifPresent(header -> {
            final String[] keyValuePairs = header.split("; ");
            stream(keyValuePairs)
                    .map(Cookies::parseKeyValuePair)
                    .forEach(entry -> cookies.put(entry.getKey(), entry.getValue()));
        });
        return new Cookies(cookies);
    }

    public Optional<String> getOptionalCookie(final String name) {
        final CookieName cookieName = cookieName(name);
        return getOptionally(cookies, cookieName)
                .map(CookieValue::stringValue);
    }

    public String getCookie(final String name) {
        return getOptionalCookie(name)
                .orElseThrow(() -> new RuntimeException(format("No cookie with name '%s'", name)));
    }

    private static Map.Entry<CookieName, CookieValue> parseKeyValuePair(final String keyValuePair) {
        final String[] tokens = keyValuePair.split("=");
        final String value;
        if (tokens.length == 1) {
            value = "";
        } else {
            value = tokens[1];
        }
        final String adjustedValue;
        if(value.startsWith("\"") && value.endsWith("\"")) {
            adjustedValue = value.substring(1, value.length() - 1);
        } else {
            adjustedValue = value;
        }
        final String key = tokens[0];
        return new AbstractMap.SimpleEntry<>(cookieName(key), cookieValue(adjustedValue));
    }
}
