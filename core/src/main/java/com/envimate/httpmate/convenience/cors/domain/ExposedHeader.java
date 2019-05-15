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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExposedHeader {
    private static final List<ExposedHeader> SIMPLE_RESPONSE_HEADERS =
            of("Cache-Control", "Content-Language", "Content-Type", "Expires", "Last-Modified", "Pragma")
            .map(ExposedHeader::exposedHeader)
            .collect(toList());

    private final String headerName;

    public static ExposedHeader exposedHeader(final String headerName) {
        validateNotNullNorEmpty(headerName, "headerName");
        final String normalized = headerName.toLowerCase();
        return new ExposedHeader(normalized);
    }

    public boolean isSimpleHeader() {
       return SIMPLE_RESPONSE_HEADERS.contains(this);
    }

    public String internalValueForMapping() {
        return headerName;
    }
}
