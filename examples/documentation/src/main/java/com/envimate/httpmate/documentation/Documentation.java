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

package com.envimate.httpmate.documentation;

import com.envimate.httpmate.HttpMate;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public final class Documentation {

    public static void main(final String[] args) {
        anHttpMate()
                .get("/hello", (request, response) -> response.setBody("hi!"))
                .get("/ping", PingUseCase.class)
                .build();

        final HttpMate httpMate = anHttpMate()
                .get("/", (request, response) -> {
                    response.setStatus(404);
                })
                .build();
        pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
    }
}
