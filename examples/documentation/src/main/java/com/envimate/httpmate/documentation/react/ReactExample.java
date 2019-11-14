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

package com.envimate.httpmate.documentation.react;

import com.envimate.httpmate.HttpMate;
import com.envimate.mapmate.builder.MapMate;
import com.google.gson.Gson;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.cors.CorsConfigurators.toActivateCORSWithoutValidatingTheOrigin;
import static com.envimate.httpmate.documentation.react.LoginException.loginException;
import static com.envimate.httpmate.exceptions.ExceptionConfigurators.toMapExceptionsOfType;
import static com.envimate.httpmate.http.Http.StatusCodes.BAD_REQUEST;
import static com.envimate.httpmate.mapmate.MapMateConfigurator.toUseMapMate;
import static com.envimate.httpmate.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static com.envimate.httpmate.security.SecurityConfigurators.toAuthenticateUsingOAuth2BearerToken;
import static com.envimate.httpmate.security.SecurityConfigurators.toAuthorizeAllAuthenticatedRequests;
import static com.envimate.mapmate.builder.MapMate.aMapMate;
import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.parser;
import static io.jsonwebtoken.security.Keys.secretKeyFor;
import static java.util.Objects.isNull;

public final class ReactExample {
    private static final Map<String, String> userDatabase = Map.of("joe", hashPassword("foo"));

    private static final Key key = secretKeyFor(SignatureAlgorithm.HS256);
    private static final JwtParser jwtParser = parser().setSigningKey(key);

    public static void main(String[] args) {
        final Gson gson = new Gson();

        final MapMate mapMate = aMapMate()
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .build();

        final HttpMate httpMate = anHttpMate()
                .post("/login", (request, response) -> {
                    final Map<String, Object> bodyMap = request.bodyMap();
                    final String username = (String) bodyMap.get("username");
                    final String password = (String) bodyMap.get("password");

                    checkCredentials(username, password);

                    final String jws = builder()
                            .setSubject(username)
                            .signWith(key).compact();

                    response.setBody(Map.of("token", jws));
                })
                .get("/dashboard", (request, response) -> response.setBody(Map.of("message", new Date().toString())))
                .configured(toUseMapMate(mapMate))
                .configured(toActivateCORSWithoutValidatingTheOrigin())
                .configured(toAuthenticateUsingOAuth2BearerToken(ReactExample::checkJwt))
                .configured(toAuthorizeAllAuthenticatedRequests().exceptRequestsTo("/login"))
                .configured(toMapExceptionsOfType(LoginException.class, (exception, response) -> {
                    response.setBody(Map.of(
                            "errorType", "LOGIN",
                            "errorMessage", exception.getMessage()
                    ));
                    response.setStatus(BAD_REQUEST);
                }))
                .build();

        System.out.println("httpMate = " + httpMate.dumpChains());

        pureJavaEndpointFor(httpMate).listeningOnThePort(1300);

    }

    private static Optional<String> checkJwt(final String token) {
        try {
            return Optional.ofNullable(jwtParser.parseClaimsJws(token).getBody().getSubject());
        } catch (final JwtException e) {
            return Optional.empty();
        }
    }

    private static void checkCredentials(final String username, final String password) {
        if (isNull(username) || username.isEmpty()) {
            throw loginException("username is empty");
        }
        if (isNull(password) || password.isEmpty()) {
            throw loginException("password is empty");
        }
        if (!userDatabase.containsKey(username)) {
            throw loginException("login failed");
        }
        final String hash = userDatabase.get(username);
        if (!hash.equals(hashPassword(password))) {
            throw loginException("login failed");
        }
    }

    private static String hashPassword(final String password) {
        return password;
    }
}
