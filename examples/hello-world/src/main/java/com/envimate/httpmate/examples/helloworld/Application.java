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
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.convenience.configurators.Configurators;
import com.envimate.httpmate.convenience.endpoints.PureJavaEndpoint;
import com.envimate.httpmate.convenience.handler.HttpHandler;
import com.envimate.httpmate.convenience.handler.HttpRequest;
import com.envimate.httpmate.convenience.handler.HttpResponse;
import com.envimate.httpmate.handler.Handler;
import com.envimate.httpmate.http.HttpRequestMethod;

import java.util.Optional;

import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.http.Http.StatusCodes.*;

public final class Application {

    private static final int PORT = 1337;

    private Application() {
    }

    public static void main(final String[] args) {
        final HttpMate httpMate = HttpMate.aLowLevelHttpMate()
                .callingTheHandler(new HttpHandler() {
                    @Override
                    public void handle(final HttpRequest request, final HttpResponse httpResponse) {
                        final Optional<String> name = request.queryParameters().getQueryParameter("name");
                        httpResponse.setBody("Hello " + name.orElse("World"));
                        httpResponse.setStatus(OK);
                    }
                })
                .forRequestPath("/api/hello")
                .andRequestMethod(HttpRequestMethod.GET)
                .callingTheHandler(new Handler() {
                    @Override
                    public void handle(final MetaData metaData) {
                        final Optional<String> name = metaData.get(QUERY_PARAMETERS).getQueryParameter("name");
                        metaData.set(RESPONSE_STRING, "Hello " + name.orElse("World!"));
                        metaData.set(RESPONSE_STATUS, OK);
                    }
                })
                .forRequestPath("/api/helloDirect")
                .andRequestMethod(HttpRequestMethod.GET)
                .thatIs()
                .configured(Configurators.toLogUsing((message, metaData) -> {
                    System.out.println(message);
                }))
                .build();

        PureJavaEndpoint.pureJavaEndpointFor(httpMate).listeningOnThePort(PORT);
    }
}
