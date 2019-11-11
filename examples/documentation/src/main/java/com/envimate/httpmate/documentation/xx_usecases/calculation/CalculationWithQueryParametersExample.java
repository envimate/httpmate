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

package com.envimate.httpmate.documentation.xx_usecases.calculation;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.documentation.xx_usecases.calculation.domain.MultiplicationRequest;
import com.envimate.httpmate.documentation.xx_usecases.calculation.usecases.MultiplicationUseCase;
import com.envimate.mapmate.builder.MapMate;
import com.google.gson.Gson;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.events.EventConfigurators.toEnrichTheIntermediateMapWithAllQueryParameters;
import static com.envimate.httpmate.mapmate.MapMateConfigurator.toUseMapMate;
import static com.envimate.httpmate.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static com.envimate.mapmate.builder.MapMate.aMapMate;
import static com.envimate.mapmate.builder.recipes.primitives.BuiltInPrimitiveSerializedAsStringSupport.builtInPrimitiveSerializedAsStringSupport;

public final class CalculationWithQueryParametersExample {

    public static void main(final String[] args) {
        final Gson gson = new Gson();
        final MapMate mapMate = aMapMate(MultiplicationRequest.class.getPackageName())
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .usingRecipe(builtInPrimitiveSerializedAsStringSupport())
                .build();

        final HttpMate httpMate = anHttpMate()
                .get("/multiply", MultiplicationUseCase.class)
                .configured(toEnrichTheIntermediateMapWithAllQueryParameters())
                .configured(toUseMapMate(mapMate))
                .build();
        pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
    }
}
