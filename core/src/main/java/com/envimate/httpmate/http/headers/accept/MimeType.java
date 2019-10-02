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

package com.envimate.httpmate.http.headers.accept;

import com.envimate.httpmate.http.headers.HeaderValueWithComment;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.envimate.httpmate.http.headers.HeaderValueWithComment.fromString;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MimeType {
    private static final Pattern PATTERN = Pattern.compile("(?<type>[^/]*)(/(?<subtype>.*))?");

    private final String type;
    private final String subtype;

    static MimeType parseMimeType(final String string) {
        validateNotNullNorEmpty(string, "string");
        final HeaderValueWithComment value = fromString(string);

        final String typeAndSubtype = value.value();
        final Matcher matcher = PATTERN.matcher(typeAndSubtype);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(format("Mimetype '%s' must match '%s'", typeAndSubtype, PATTERN.pattern()));
        }
        final String type = matcher.group("type").toLowerCase();
        final String subtype = ofNullable(matcher.group("subtype")).orElse("");

        return new MimeType(type, subtype);
    }

    public String type() {
        return type;
    }

    public String subtype() {
        return subtype;
    }
}
