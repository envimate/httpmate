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

package com.envimate.httpmate.awslambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.chains.MetaDataKey;
import com.envimate.httpmate.logger.LoggerImplementation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.awslambda.AwsLambdaLogger.awsLambdaLogger;
import static com.envimate.httpmate.chains.MetaData.emptyMetaData;
import static com.envimate.httpmate.chains.MetaDataKey.metaDataKey;
import static com.envimate.httpmate.util.Maps.mapToMultiMap;
import static com.envimate.httpmate.util.Streams.inputStreamToString;
import static com.envimate.httpmate.util.Streams.stringToInputStream;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwsLambdaEndpoint {

    public static final MetaDataKey<Context> CONTEXT_KEY = metaDataKey("awsLambdaContext");

    private final HttpMate httpMate;

    public static LoggerImplementation awsLogger() {
        return awsLambdaLogger();
    }

    public static AwsLambdaEndpoint awsLambdaEndpointFor(final HttpMate httpMate) {
        validateNotNull(httpMate, "httpMate");
        return new AwsLambdaEndpoint(httpMate);
    }

    public APIGatewayProxyResponseEvent delegate(final APIGatewayProxyRequestEvent event, final Context context) {
        final String httpRequestMethod = event.getHttpMethod();
        final String path = event.getPath();
        final Map<String, String> headers = event.getHeaders();
        final String body = ofNullable(event.getBody()).orElse("");
        final InputStream bodyStream = stringToInputStream(body);
        final Map<String, String> queryParameters = ofNullable(event.getQueryStringParameters()).orElseGet(HashMap::new);

        final MetaData metaData = emptyMetaData();
        metaData.set(RAW_REQUEST_HEADERS, mapToMultiMap(headers));
        metaData.set(RAW_REQUEST_QUERY_PARAMETERS, queryParameters);
        metaData.set(RAW_METHOD, httpRequestMethod);
        metaData.set(RAW_PATH, path);
        metaData.set(REQUEST_BODY_STREAM, bodyStream);
        metaData.set(CONTEXT_KEY, context);
        metaData.set(IS_HTTP_REQUEST, true);

        httpMate.handleRequest(metaData, response -> {
            throw new UnsupportedOperationException();
        });

        final int statusCode = metaData.get(RESPONSE_STATUS);
        final Map<String, String> responseHeaders = metaData.get(RESPONSE_HEADERS);
        final InputStream responseStream = metaData.get(RESPONSE_STREAM);
        final String responseBody = inputStreamToString(responseStream);
        return new APIGatewayProxyResponseEvent().withStatusCode(statusCode).withHeaders(responseHeaders).withBody(responseBody);
    }
}
