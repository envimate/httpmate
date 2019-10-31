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

import com.envimate.httpmate.handler.http.HttpRequest;
import com.envimate.httpmate.security.authorization.AuthorizationHeader;
import com.envimate.httpmate.security.authentication.Authenticator;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.envimate.httpmate.http.Http.Headers.AUTHORIZATION;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.regex.Pattern.compile;

public interface BasicAuthAuthenticator extends Authenticator<HttpRequest> {
    Pattern PATTERN = compile("(?<username>[^:]+):(?<password>.*)");

    @Override
    default Optional<?> authenticate(final HttpRequest request) {
        return request.headers().getOptionalHeader(AUTHORIZATION)
                .flatMap(AuthorizationHeader::parse)
                .filter(authorizationHeader -> authorizationHeader.type().equals("Basic"))
                .map(AuthorizationHeader::credentials)
                .map(Base64Decoder::decodeBase64)
                .map(PATTERN::matcher)
                .filter(Matcher::matches)
                .flatMap(matcher -> {
                    final String username = matcher.group("username");
                    final String password = matcher.group("password");
                    if (isAuthenticated(username, password)) {
                        return of(username);
                    } else {
                        return empty();
                    }
                });
    }

    boolean isAuthenticated(String username, String password);
}
