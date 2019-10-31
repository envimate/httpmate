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

package com.envimate.httpmate.cors;

import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.chains.Configurator;
import com.envimate.httpmate.chains.DependencyRegistry;
import com.envimate.httpmate.cors.domain.ExposedHeader;
import com.envimate.httpmate.cors.domain.ExposedHeaders;
import com.envimate.httpmate.cors.domain.MaxAge;
import com.envimate.httpmate.cors.domain.RequestedHeader;
import com.envimate.httpmate.cors.policy.AllowedHeaders;
import com.envimate.httpmate.cors.policy.AllowedMethods;
import com.envimate.httpmate.cors.policy.AllowedOrigins;
import com.envimate.httpmate.cors.policy.ResourceSharingPolicy;
import com.envimate.httpmate.http.HttpRequestMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.envimate.httpmate.cors.CorsModule.corsModule;
import static com.envimate.httpmate.cors.domain.ExposedHeaders.exposedHeaders;
import static com.envimate.httpmate.cors.domain.MaxAge.maxAgeInSeconds;
import static com.envimate.httpmate.cors.domain.MaxAge.undefinedMaxAge;
import static com.envimate.httpmate.cors.policy.ResourceSharingPolicy.resourceSharingPolicy;
import static com.envimate.httpmate.http.HttpRequestMethod.GET;
import static com.envimate.httpmate.http.HttpRequestMethod.POST;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CorsConfigurator implements Configurator {
    private final AllowedOrigins allowedOrigins;
    private volatile AllowedMethods allowedMethods;
    private volatile AllowedHeaders allowedHeaders;
    private volatile ExposedHeaders exposedHeaders;
    private volatile boolean credentialsSupport = false;
    private volatile MaxAge maxAge = undefinedMaxAge();

    public static CorsConfigurator corsConfigurator(final AllowedOrigins allowedOrigins) {
        validateNotNull(allowedOrigins, "allowedOrigins");
        final CorsConfigurator corsConfigurator = new CorsConfigurator(allowedOrigins);
        return corsConfigurator
                .withAllowedMethods(GET, POST)
                .withAllowedHeaders()
                .exposingTheResponseHeaders();
    }

    public CorsConfigurator withAllowedMethods(final HttpRequestMethod... methods) {
        validateNotNull(methods, "methods");
        final List<HttpRequestMethod> methodList = asList(methods);
        allowedMethods = requestedMethod -> methodList.parallelStream().anyMatch(requestedMethod::matches);
        return this;
    }

    public CorsConfigurator allowingAllHeaders() {
        this.allowedHeaders = requestedHeader -> true;
        return this;
    }

    public CorsConfigurator withAllowedHeaders(final String... headers) {
        validateNotNull(headers, "headers");
        final List<RequestedHeader> allowedHeaders = Arrays.stream(headers)
                .map(RequestedHeader::requestedHeader)
                .collect(toList());
        this.allowedHeaders = allowedHeaders::contains;
        return this;
    }

    public CorsConfigurator exposingTheResponseHeaders(final String... exposedHeaders) {
        this.exposedHeaders = exposedHeaders(stream(exposedHeaders)
                .map(ExposedHeader::exposedHeader)
                .collect(toList()));
        return this;
    }

    public CorsConfigurator exposingAllResponseHeaders() {
        return exposingTheResponseHeaders("*");
    }

    public CorsConfigurator allowingCredentials() {
        credentialsSupport = true;
        return this;
    }

    public CorsConfigurator notAllowingCredentials() {
        credentialsSupport = false;
        return this;
    }

    public CorsConfigurator withTimeOutAfter(final int timeout, final TimeUnit timeUnit) {
        final long seconds = timeUnit.toSeconds(timeout);
        maxAge = maxAgeInSeconds(seconds);
        return this;
    }

    @Override
    public List<ChainModule> supplyModulesIfNotAlreadyPreset() {
        return singletonList(corsModule());
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final CorsModule corsModule = dependencyRegistry.getDependency(CorsModule.class);
        final ResourceSharingPolicy resourceSharingPolicy = resourceSharingPolicy(
                allowedOrigins, allowedMethods, allowedHeaders, exposedHeaders, credentialsSupport, maxAge);
        corsModule.setResourceSharingPolicy(resourceSharingPolicy);
    }
}
