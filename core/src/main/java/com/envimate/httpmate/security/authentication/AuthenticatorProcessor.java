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

package com.envimate.httpmate.security.authentication;

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.chains.Processor;
import com.envimate.httpmate.handler.http.HttpRequest;
import com.envimate.httpmate.security.Filter;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

import static com.envimate.httpmate.HttpMateChainKeys.AUTHENTICATION_INFORMATION;
import static com.envimate.httpmate.handler.http.HttpRequest.httpRequest;
import static com.envimate.httpmate.security.authentication.CouldNotAuthenticateException.couldNotAuthenticateException;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthenticatorProcessor implements Processor {
    private final Authenticator<MetaData> authenticator;
    private final AuthenticatorId authenticatorId;
    private final List<Filter> optionalRequests;

    public static AuthenticatorProcessor authenticatorProcessor(final Authenticator<MetaData> authenticator,
                                                                final AuthenticatorId authenticatorId,
                                                                final List<Filter> optionalRequests) {
        validateNotNull(authenticator, "authenticator");
        validateNotNull(authenticatorId, "authenticatorId");
        validateNotNull(optionalRequests, "optionalRequests");
        return new AuthenticatorProcessor(authenticator, authenticatorId, optionalRequests);
    }

    @Override
    public void apply(final MetaData metaData) {
        final Optional<?> authenticationInformation = authenticator.authenticate(metaData);
        if (authenticationInformation.isEmpty()) {
            failIfNotOptional(metaData);
        } else {
            authenticationInformation.ifPresent(information -> metaData.set(AUTHENTICATION_INFORMATION, information));
        }
    }

    private void failIfNotOptional(final MetaData metaData) {
        final HttpRequest request = httpRequest(metaData);
        if (optionalRequests.stream()
                .noneMatch(filter -> filter.filter(request))) {
            throw couldNotAuthenticateException(metaData, authenticatorId);
        }
    }
}
