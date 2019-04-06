/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.awslambda.AwsLambdaEndpoint;
import com.envimate.httpmate.examples.awslambda.usecases.HelloUseCase;
import com.envimate.httpmate.request.HttpRequestMethod;
import com.google.gson.Gson;

import static com.envimate.httpmate.awslambda.AwsLambdaEndpoint.awsLambdaEndpointFor;

public class HttpMateIntegration implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final AwsLambdaEndpoint ENDPOINT;

    static {
        final Gson gson = new Gson();
        ENDPOINT = null;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent event, final Context context) {
        return ENDPOINT.delegate(event, context);
    }
}
