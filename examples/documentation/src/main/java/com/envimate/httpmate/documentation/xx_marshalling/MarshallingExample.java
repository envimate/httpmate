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

package com.envimate.httpmate.documentation.xx_marshalling;

import com.envimate.httpmate.HttpMate;
import com.google.gson.Gson;

import java.util.Map;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.http.headers.ContentType.fromString;
import static com.envimate.httpmate.marshalling.MarshallingModule.toMarshallBodiesBy;
import static com.envimate.httpmate.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;

public final class MarshallingExample {
    private static final Gson GSON = new Gson();

    @SuppressWarnings("unchecked")
    public static void main(final String[] args) {
        final HttpMate httpMate = anHttpMate()
                .configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("application/json")).with(body -> GSON.fromJson(body, Map.class))
                        .marshallingContentTypeInResponses(fromString("application/json")).with(map -> GSON.toJson(map))
                        .usingTheDefaultContentType(fromString("application/json")))
                .build();
        pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
    }
}
