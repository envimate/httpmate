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

package com.envimate.httpmate.security.basicauth;

import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.chains.DependencyRegistry;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.handler.http.HttpRequest;
import com.envimate.httpmate.security.Filter;
import com.envimate.httpmate.security.authentication.AuthenticatorProcessor;
import com.envimate.httpmate.security.authorization.AuthorizerConfigurator;
import com.envimate.httpmate.security.authorization.HttpAuthorizer;
import com.envimate.httpmate.security.config.SecurityConfigurator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.HttpMateChainKeys.AUTHENTICATION_INFORMATION;
import static com.envimate.httpmate.handler.http.HttpRequest.httpRequest;
import static com.envimate.httpmate.http.Http.Headers.WWW_AUTHENTICATE;
import static com.envimate.httpmate.http.Http.StatusCodes.UNAUTHORIZED;
import static com.envimate.httpmate.security.authentication.AuthenticatorProcessor.authenticatorProcessor;
import static com.envimate.httpmate.security.authorization.AuthorizerConfigurator.authorizerConfigurator;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BasicAuthConfigurator implements SecurityConfigurator<BasicAuthConfigurator> {
    private final AuthorizerConfigurator authorizerConfigurator;
    private volatile String realm;

    public static BasicAuthConfigurator basicAuthenticationConfigurator(final BasicAuthAuthenticator authenticator) {
        final AuthenticatorProcessor authenticatorProcessor = authenticatorProcessor(metaData -> {
            final HttpRequest request = httpRequest(metaData);
            return authenticator.authenticate(request);
        });

        final HttpAuthorizer authorizer = (authenticationInformation, request) -> {
            final MetaData metaData = request.getMetaData();
            authenticatorProcessor.apply(metaData);
            return metaData.getOptional(AUTHENTICATION_INFORMATION).isPresent();
        };

        final AuthorizerConfigurator authorizerConfigurator = authorizerConfigurator(authorizer);
        return new BasicAuthConfigurator(authorizerConfigurator);
    }

    @Override
    public BasicAuthConfigurator inPhase(final ChainName phase) {
        authorizerConfigurator.inPhase(phase);
        return this;
    }

    @Override
    public BasicAuthConfigurator onlyRequestsThat(final Filter filter) {
        authorizerConfigurator.onlyRequestsThat(filter);
        return this;
    }

    public BasicAuthConfigurator withMessage(final String message) {
        validateNotNull(message, "message");
        realm = message;
        return this;
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        authorizerConfigurator.rejectingUnauthorizedRequestsUsing((request, response) -> {
            final StringBuilder headerBuilder = new StringBuilder();
            headerBuilder.append("Basic");
            if(nonNull(realm)) {
                headerBuilder.append(format(" realm=\"%s\"", realm));
            }
            response.addHeader(WWW_AUTHENTICATE, headerBuilder.toString());
            response.setStatus(UNAUTHORIZED);
        });
        authorizerConfigurator.configure(dependencyRegistry);
    }
}
