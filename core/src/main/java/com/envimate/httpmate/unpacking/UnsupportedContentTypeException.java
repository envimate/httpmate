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

package com.envimate.httpmate.unpacking;

import com.envimate.httpmate.request.ContentType;

import java.util.Collection;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public final class UnsupportedContentTypeException extends RuntimeException {

    private UnsupportedContentTypeException(final String message) {
        super(message);
    }

    static UnsupportedContentTypeException unsupportedContentTypeException(
            final ContentType contentType,
            final Collection<ContentType> supportedContentTypes) {
        validateNotNull(contentType, "contentType");
        validateNotNull(supportedContentTypes, "supportedContentTypes");
        final String supported = supportedContentTypes.stream()
                .map(ContentType::internalValueForMapping)
                .collect(joining(", ", "'", "'"));
        return new UnsupportedContentTypeException(format(
                "Content type '%s' is not supported; supported content types are: %s",
                contentType.internalValueForMapping(), supported
        ));
    }
}
