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

package com.envimate.httpmate.spark;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.chains.MetaData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.envimate.httpmate.chains.HttpMateChainKeys.*;
import static com.envimate.httpmate.chains.MetaData.emptyMetaData;
import static com.envimate.httpmate.util.Streams.streamInputStreamToOutputStream;
import static com.envimate.httpmate.util.Streams.stringToInputStream;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class SparkRouteWebserviceAdapter implements Route {
    private final HttpMate httpMate;

    @Override
    public Object handle(final Request request, final Response sparkResponse) {
        final String httpRequestMethod = request.requestMethod();
        final String path = request.pathInfo();
        final Map<String, String> headers = request.headers().stream()
                .collect(toMap(key -> key, request::headers));
        final Map<String, String> queryParameters = extractQueryParameters(request);

        final InputStream body;
        try {
            body = request.raw().getInputStream();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final MetaData metaData = emptyMetaData();
        metaData.set(RAW_HEADERS, headers);
        metaData.set(RAW_QUERY_PARAMETERS, queryParameters);
        metaData.set(RAW_METHOD, httpRequestMethod);
        metaData.set(PATH, path);
        metaData.set(BODY_STREAM, body);
        metaData.set(IS_HTTP_REQUEST, true);

        this.httpMate.handleRequest(metaData, rawResponse -> {
            final Map<String, String> responseHeaders = metaData.get(RESPONSE_HEADERS);
            responseHeaders.forEach(sparkResponse::header);
            final int responseStatus = metaData.get(RESPONSE_STATUS);
            sparkResponse.status(responseStatus);
            final OutputStream outputStream = sparkResponse.raw().getOutputStream();
            final InputStream responseBody = metaData.getOptional(STREAM_RESPONSE).orElseGet(() -> stringToInputStream(""));
            streamInputStreamToOutputStream(responseBody, outputStream);
        });

        return null;
    }

    private static Map<String, String> extractQueryParameters(final Request request) {
        final Set<String> queryParametersSet = request.queryParams();
        final Map<String, String> queryParameters = new HashMap<>();
        for (final String parameter : queryParametersSet) {
            final String[] values = request.queryParamsValues(parameter);
            final Set<String> valueSet = stream(values).collect(toSet());
            if (valueSet.isEmpty()) {
                queryParameters.put(parameter, null);
            } else {
                queryParameters.put(parameter, valueSet.iterator().next());
            }
        }
        return queryParameters;
    }
}
