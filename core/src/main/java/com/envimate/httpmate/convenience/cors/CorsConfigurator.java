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

package com.envimate.httpmate.convenience.cors;

import com.envimate.httpmate.convenience.cors.builder.MethodsStage;
import com.envimate.httpmate.convenience.cors.domain.ExposedHeader;
import com.envimate.httpmate.convenience.cors.domain.ExposedHeaders;
import com.envimate.httpmate.convenience.cors.policy.ResourceSharingPolicy;

import static com.envimate.httpmate.chains.Configurator.toUseModules;
import static com.envimate.httpmate.convenience.cors.CorsModule.corsModule;
import static com.envimate.httpmate.convenience.cors.domain.ExposedHeaders.exposedHeaders;
import static com.envimate.httpmate.convenience.cors.policy.ResourceSharingPolicy.resourceSharingPolicy;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public final class CorsConfigurator {

    private CorsConfigurator() {
    }

    public static MethodsStage toProtectAjaxRequestsAgainstCsrfAttacksByTellingTheBrowserThatRequests() {
        return allowedMethods -> allowedOrigins -> allowedHeaders -> exposedHeaders -> credentialsSupport -> maximumAge -> {
            validateNotNull(allowedMethods, "allowedMethods");
            validateNotNull(allowedOrigins, "allowedOrigins");
            validateNotNull(allowedHeaders, "allowedHeaders");
            validateNotNull(exposedHeaders, "exposedHeaders");
            validateNotNull(maximumAge, "maximumAge");
            final ExposedHeaders exposedHeadersObject = exposedHeaders(stream(exposedHeaders)
                    .map(ExposedHeader::exposedHeader)
                    .collect(toList()));
            final ResourceSharingPolicy resourceSharingPolicy = resourceSharingPolicy(
                    allowedOrigins, allowedMethods, allowedHeaders, exposedHeadersObject, credentialsSupport, maximumAge);
            return toUseModules(corsModule(resourceSharingPolicy));
        };
    }
}
