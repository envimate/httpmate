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

package com.envimate.httpmate.documentation.form;

import com.envimate.httpmate.HttpMate;
import com.envimate.mapmate.builder.MapMate;

import java.util.Map;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static com.envimate.httpmate.mapmate.MapMateConfigurator.toUseMapMate;
import static com.envimate.mapmate.builder.MapMate.aMapMate;
import static com.envimate.mapmate.builder.recipes.marshallers.urlencoded.UrlEncodedMarshallerRecipe.urlEncodedMarshaller;

public final class FormExample {

    public static void main(String[] args) {
        final MapMate mapMate = aMapMate()
                .usingRecipe(urlEncodedMarshaller())
                .build();
        final HttpMate httpMate = anHttpMate()
                .get("/form", (request, response) -> response.setJavaResourceAsBody("form.html"))
                .post("/submit", (request, response) -> {
                    final Map<String, Object> bodyMap = request.bodyMap();
                    final String name = (String) bodyMap.get("name");
                    final String profession = (String) bodyMap.get("profession");
                    response.setBody("Hello " + name + " and good luck as a " + profession + "!");
                })
                .configured(toUseMapMate(mapMate))
                .build();
        pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
    }
}
