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

package com.envimate.httpmate.cors.policy;

import com.envimate.httpmate.cors.domain.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceSharingPolicy {
    private final AllowedOrigins allowedOrigins;
    private final AllowedMethods allowedMethods;
    private final AllowedHeaders allowedHeaders;
    private final ExposedHeaders exposedHeaders;
    private final boolean supportsCredentials;
    private final MaxAge maxAge;

    public static ResourceSharingPolicy resourceSharingPolicy(final AllowedOrigins allowedOrigins,
                                                              final AllowedMethods allowedMethods,
                                                              final AllowedHeaders allowedHeaders,
                                                              final ExposedHeaders exposedHeaders,
                                                              final boolean supportsCredentials,
                                                              final MaxAge maxAge) {
        validateNotNull(allowedOrigins, "allowedOrigins");
        validateNotNull(allowedMethods, "allowedMethods");
        validateNotNull(allowedHeaders, "allowedHeaders");
        validateNotNull(exposedHeaders, "exposedHeaders");
        validateNotNull(maxAge, "maxAge");
        return new ResourceSharingPolicy(
                allowedOrigins, allowedMethods, allowedHeaders, exposedHeaders, supportsCredentials, maxAge);
    }

    public boolean validateOrigin(final Origin origin) {
        validateNotNull(origin, "origin");
        return allowedOrigins.isAllowed(origin);
    }

    public boolean validateRequestedMethod(final RequestedMethod requestedMethod) {
        validateNotNull(requestedMethod, "requestedMethod");
        return allowedMethods.isAllowed(requestedMethod);
    }

    public boolean validateRequestedHeaders(final RequestedHeaders requestedHeaders) {
        validateNotNull(requestedHeaders, "requestedHeaders");
        for (final RequestedHeader header : requestedHeaders.headers()) {
            if (!allowedHeaders.isAllowed(header)) {
                return false;
            }
        }
        return true;
    }

    public boolean supportsCredentials() {
        return supportsCredentials;
    }

    public ExposedHeaders exposedHeaders() {
        return exposedHeaders;
    }

    public MaxAge maxAge() {
        return maxAge;
    }
}
