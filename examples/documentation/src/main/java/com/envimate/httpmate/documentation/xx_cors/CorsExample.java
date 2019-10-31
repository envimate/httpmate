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

package com.envimate.httpmate.documentation.xx_cors;

import com.envimate.httpmate.HttpMate;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.cors.CorsConfigurators.toActivateCORSWithAllowedOrigins;
import static com.envimate.httpmate.http.HttpRequestMethod.PUT;
import static com.envimate.httpmate.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public final class CorsExample {

    public static void main(final String[] args) {
        final HttpMate httpMate = anHttpMate()
                .put("/api", (request, response) -> response.setBody("Version 1.0"))
                .configured(toActivateCORSWithAllowedOrigins("frontend.example.org").withAllowedMethods(PUT))
                .build();
        pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
    }
}
