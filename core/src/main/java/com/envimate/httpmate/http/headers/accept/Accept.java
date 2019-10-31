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

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.http.headers.ContentType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.HttpMateChainKeys.REQUEST_HEADERS;
import static com.envimate.httpmate.http.headers.accept.MimeType.parseMimeType;
import static com.envimate.httpmate.http.headers.accept.MimeTypeMatcher.parseMimeTypeMatcher;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Accept {
    private final MimeTypeMatcher mimeTypeMatcher;

    public static Accept fromMetaData(final MetaData metaData) {
        return metaData.getOptional(REQUEST_HEADERS).map(headers -> {
            final String header = headers.getOptionalHeader("Accept").orElse("*/*");
            final MimeTypeMatcher mimeTypeMatcher = parseMimeTypeMatcher(header);
            return new Accept(mimeTypeMatcher);
        }).orElseGet(() -> new Accept(parseMimeTypeMatcher("*/*")));
    }

    public boolean contentTypeIsAccepted(final ContentType contentType) {
        final MimeType mimeType = parseMimeType(contentType.internalValueForMapping());
        return mimeTypeMatcher.matches(mimeType);
    }
}
