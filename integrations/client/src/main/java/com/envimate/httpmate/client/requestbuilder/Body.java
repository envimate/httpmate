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

package com.envimate.httpmate.client.requestbuilder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Body {
    private final Supplier<InputStream> inputStream;
    private final String contentType;

    public static Body bodyWithoutContentType(final Supplier<InputStream> inputStream) {
        return new Body(inputStream, null);
    }

    public static Body bodyWithContentType(final Supplier<InputStream> inputStream,
                                           final String contentType) {
        return new Body(inputStream, contentType);
    }

    public InputStream inputStream() {
        return this.inputStream.get();
    }

    public Optional<String> contentType() {
        return ofNullable(this.contentType);
    }
}
