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

package com.envimate.httpmate.documentation.authentication;

import com.envimate.httpmate.HttpMate;
import com.envimate.mapmate.builder.MapMate;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.Key;
import java.util.Map;
import java.util.Optional;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.mapmate.MapMateConfigurator.toUseMapMate;
import static com.envimate.httpmate.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static com.envimate.httpmate.security.SecurityConfigurators.*;
import static com.envimate.mapmate.builder.MapMate.aMapMate;
import static com.envimate.mapmate.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe.urlEncodedMarshaller;
import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.parser;
import static io.jsonwebtoken.security.Keys.secretKeyFor;

public final class CustomLoginExample {

    public static void main(final String[] args) {
        final Key key = secretKeyFor(SignatureAlgorithm.HS256);
        final JwtParser jwtParser = parser().setSigningKey(key);

        final MapMate mapMate = aMapMate()
                .usingRecipe(urlEncodedMarshaller())
                .build();
        final UserDatabase userDatabase = new InMemoryUserDatabase();
        final HttpMate httpMate = anHttpMate()
                .get("/login", (request, response) -> response.setJavaResourceAsBody("login.html"))
                .post("/login", (request, response) -> {
                    final Map<String, Object> loginForm = request.bodyMap();
                    final String username = (String) loginForm.get("username");
                    final String password = (String) loginForm.get("password");
                    if (!userDatabase.authenticate(username, password)) {
                        throw new RuntimeException("Login failed");
                    }
                    final boolean admin = userDatabase.hasAdminRights(username);
                    final String jwt = builder()
                            .setSubject(username)
                            .claim("admin", admin)
                            .signWith(key).compact();
                    response.setCookie("jwt", jwt);
                })
                .get("/normal", (request, response) -> response.setBody("The normal section"))
                .get("/admin", (request, response) -> response.setBody("The admin section"))
                .configured(toAuthenticateUsingCookie("jwt", jwt -> Optional.of(jwtParser.parseClaimsJws(jwt).getBody())))
                .configured(toAuthorizeAllAuthenticatedRequests().exceptRequestsTo("/login"))
                .configured(toAuthorizeRequestsUsing((authenticationInformation, request) -> authenticationInformation
                        .map(object -> (Claims) object)
                        .map(claims -> (Boolean) claims.get("admin"))
                        .orElse(false))
                        .onlyRequestsTo("/admin")
                        .rejectingUnauthorizedRequestsUsing((request, response) -> response.setBody("Please login as an administrator.")))
                .configured(toUseMapMate(mapMate))
                .build();
        pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
    }
}
