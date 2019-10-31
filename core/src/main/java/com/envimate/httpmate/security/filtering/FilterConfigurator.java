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

package com.envimate.httpmate.security.filtering;

import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.chains.DependencyRegistry;
import com.envimate.httpmate.handler.http.HttpHandler;
import com.envimate.httpmate.security.Filter;
import com.envimate.httpmate.security.authorization.AuthorizerConfigurator;
import com.envimate.httpmate.security.config.SecurityConfigurator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.security.authorization.AuthorizerConfigurator.authorizerConfigurator;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilterConfigurator implements SecurityConfigurator<FilterConfigurator> {
    private final AuthorizerConfigurator authorizerConfigurator;

    public static FilterConfigurator filterConfigurator(final Filter filter) {
        validateNotNull(filter, "filter");
        final AuthorizerConfigurator authorizerConfigurator = authorizerConfigurator(
                (authenticationInformation, request) -> !filter.filter(request));
        return new FilterConfigurator(authorizerConfigurator);
    }

    @Override
    public FilterConfigurator onlyRequestsThat(final Filter filter) {
        authorizerConfigurator.onlyRequestsThat(filter);
        return this;
    }

    @Override
    public FilterConfigurator inPhase(final ChainName phase) {
        authorizerConfigurator.inPhase(phase);
        return this;
    }

    public FilterConfigurator rejectingFilteredRequestsUsing(final HttpHandler rejectionHandler) {
        authorizerConfigurator.rejectingUnauthorizedRequestsUsing(rejectionHandler);
        return this;
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        authorizerConfigurator.configure(dependencyRegistry);
    }
}
