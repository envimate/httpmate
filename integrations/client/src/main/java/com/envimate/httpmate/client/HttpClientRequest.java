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

package com.envimate.httpmate.client;

import com.envimate.httpmate.client.requestbuilder.Body;
import com.envimate.httpmate.client.requestbuilder.PathStage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.envimate.httpmate.client.HeaderKey.headerKey;
import static com.envimate.httpmate.client.HeaderValue.headerValue;
import static com.envimate.httpmate.client.HttpClientRequestBuilder.httpClientRequestBuilderImplementation;
import static com.envimate.httpmate.client.Query.parse;
import static com.envimate.httpmate.convenience.Http.Headers.CONTENT_TYPE;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientRequest<T> {
    private final String path;
    private final String method;
    private final Map<HeaderKey, HeaderValue> headers;
    private final Map<QueryParameterKey, QueryParameterValue> queryParameters;
    private final InputStream body;
    private final Class<T> targetType;

    public static PathStage aGetRequest() {
        return aRequestOfTheMethod("GET");
    }

    public static PathStage aPostRequest() {
        return aRequestOfTheMethod("POST");
    }

    public static PathStage aPutRequest() {
        return aRequestOfTheMethod("PUT");
    }

    public static PathStage aDeleteRequest() {
        return aRequestOfTheMethod("DELETE");
    }

    public static PathStage aOptionsRequest() {
        return aRequestOfTheMethod("OPTIONS");
    }

    public static PathStage aRequestOfTheMethod(final String method) {
        return httpClientRequestBuilderImplementation(method);
    }

    static <T> HttpClientRequest<T> httpClientRequest(final String pathWithEncodedQueryParameters,
                                                      final String method,
                                                      final Map<HeaderKey, HeaderValue> headers,
                                                      final Map<QueryParameterKey, QueryParameterValue> explicitQueryParameters,
                                                      final Body body,
                                                      final Class<T> targetType) {
        final Query query = parse(pathWithEncodedQueryParameters);
        final Map<QueryParameterKey, QueryParameterValue> allQueryParameters = new HashMap<>();
        allQueryParameters.putAll(query.encodedQueryParameters());
        allQueryParameters.putAll(explicitQueryParameters);
        final String pathWithoutEncodedQueryParameters = query.path();
        final Map<HeaderKey, HeaderValue> fixedHeaders = new HashMap<>(headers);
        body.contentType().ifPresent(contentType -> fixedHeaders.put(headerKey(CONTENT_TYPE), headerValue(contentType)));
        final InputStream bodyStream = body.inputStream();
        return new HttpClientRequest<>(pathWithoutEncodedQueryParameters,
                method, fixedHeaders, allQueryParameters, bodyStream, targetType);
    }

    public String path() {
        return this.path;
    }

    public String method() {
        return this.method;
    }

    public Map<String, String> headers() {
        final Map<String, String> stringMap = new HashMap<>();
        headers.forEach((key, value) -> stringMap.put(key.value(), value.value()));
        return stringMap;
    }

    public Map<String, String> queryParameters() {
        final Map<String, String> stringMap = new HashMap<>();
        queryParameters.forEach((key, value) -> stringMap.put(key.value(), value.value()));
        return stringMap;
    }

    public InputStream body() {
        return this.body;
    }

    Class<T> targetType() {
        return targetType;
    }
}
