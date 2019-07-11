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

package com.envimate.httpmate.examples.awslambda.infra;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.envimate.httpmate.LowLevelBuilder;
import com.envimate.httpmate.awslambda.AwsLambdaEndpoint;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.envimate.httpmate.HttpMate.anHttpMateConfiguredAs;

@SuppressWarnings("unchecked")
public class HttpMateIntegration implements RequestStreamHandler {
    private static final AwsLambdaEndpoint ENDPOINT;
    private static final ObjectMapper objectMapper;
    static {
        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ENDPOINT = AwsLambdaEndpoint.awsLambdaEndpointFor(anHttpMateConfiguredAs(LowLevelBuilder.LOW_LEVEL).get("/prod/hello", (request, response) -> {
            System.out.println("Got request "+request);
            response.setStatus(200);
            response.setBody("lalala");
        }).build());
    }

    @Override
    public void handleRequest(final InputStream input, final OutputStream output, final Context context) throws IOException {
        final APIGatewayProxyRequestEvent event = objectMapper.readValue(input, APIGatewayProxyRequestEvent.class);

        final APIGatewayProxyResponseEvent result = ENDPOINT.delegate(event, context);
        objectMapper.writeValue(output, result);
        output.close();
        input.close();
    }
}
