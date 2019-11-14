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

package com.envimate.httpmate.examples.helloworld;

import com.envimate.httpmate.HttpMate;
import com.envimate.mapmate.builder.MapMate;
import com.google.gson.Gson;

import java.util.Optional;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.cors.CorsConfigurators.toActivateCORSWithoutValidatingTheOrigin;
import static com.envimate.httpmate.http.Http.StatusCodes.OK;
import static com.envimate.httpmate.http.HttpRequestMethod.GET;
import static com.envimate.httpmate.logger.LoggerConfigurators.toLogToStdout;
import static com.envimate.httpmate.mapmate.MapMateConfigurator.toUseMapMate;
import static com.envimate.httpmate.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static com.envimate.mapmate.builder.MapMate.aMapMate;

public final class Application {

    private static final int PORT = 1337;

    private Application() {
    }

    public static void main(final String[] args) {
        final Gson gson = new Gson();
        final MapMate mapMate = aMapMate(Application.class.getPackageName())
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .build();
        final HttpMate httpMate = anHttpMate()
                .get("/api/hello", (httpRequest, httpResponse) -> {
                    final Optional<String> name = httpRequest.queryParameters().getOptionalQueryParameter("name");
                    httpResponse.setBody("Hello " + name.orElse("World"));
                    httpResponse.setStatus(OK);
                })
                .get("/api/helloUseCase", HelloWorldUseCase.class)
                .get("/api/helloDirect", metaData -> {
                    final Optional<String> name = metaData.get(QUERY_PARAMETERS).getOptionalQueryParameter("name");
                    metaData.set(REQUEST_BODY_STRING, "Hello " + name.orElse("World!"));
                    metaData.set(RESPONSE_STATUS, OK);
                })
                .configured(toActivateCORSWithoutValidatingTheOrigin()
                        .withAllowedMethods(GET)
                        .allowingCredentials())
                .configured(toUseMapMate(mapMate))
                .configured(toLogToStdout())
                .build();

        pureJavaEndpointFor(httpMate).listeningOnThePort(PORT);
    }
}
