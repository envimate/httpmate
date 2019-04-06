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

import com.envimate.httpmate.client.requestbuilder.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static com.envimate.httpmate.client.HeaderKey.headerKey;
import static com.envimate.httpmate.client.HeaderValue.headerValue;
import static com.envimate.httpmate.client.HttpClientRequest.httpClientRequest;
import static com.envimate.httpmate.client.QueryParameterKey.queryParameterKey;
import static com.envimate.httpmate.client.QueryParameterValue.queryParameterValue;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientRequestBuilder implements PathStage, BodyStage, HeadersAndQueryParametersAndMappingStage {
    private final String method;
    private String path;
    private Body body;
    private final Map<HeaderKey, HeaderValue> headers = new HashMap<>();
    private final Map<QueryParameterKey, QueryParameterValue> explicitQueryParameters = new HashMap<>();

    public static PathStage httpClientRequestBuilderImplementation(final String method) {
        return new HttpClientRequestBuilder(method);
    }

    @Override
    public BodyStage toThePath(final String path) {
        this.path = path;
        return this;
    }

    @Override
    public HeadersAndQueryParametersAndMappingStage withTheBody(final Body body) {
        this.body = body;
        return this;
    }

    @Override
    public HeadersAndQueryParametersAndMappingStage withHeader(final String key, final String value) {
        final HeaderKey headerKey = headerKey(key);
        final HeaderValue headerValue = headerValue(value);
        this.headers.put(headerKey, headerValue);
        return this;
    }

    @Override
    public HeadersAndQueryParametersAndMappingStage withQueryParameter(final String key, final String value) {
        this.explicitQueryParameters.put(queryParameterKey(key), queryParameterValue(value));
        return this;
    }

    @Override
    public <T> HttpClientRequest<T> mappedTo(final Class<T> targetType) {
        return httpClientRequest(path, method, headers, explicitQueryParameters, body, targetType);
    }
}
