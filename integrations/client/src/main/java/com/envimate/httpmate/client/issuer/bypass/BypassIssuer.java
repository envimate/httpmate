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

package com.envimate.httpmate.client.issuer.bypass;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.client.*;
import com.envimate.httpmate.client.issuer.Issuer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;

import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.chains.MetaData.emptyMetaData;
import static com.envimate.httpmate.client.RawClientResponse.rawClientResponse;
import static com.envimate.httpmate.util.Maps.mapToMultiMap;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BypassIssuer implements Issuer {
    private final HttpMate httpMate;

    public static Issuer bypassIssuer(final HttpMate httpMate) {
        validateNotNull(httpMate, "httpMate");
        return new BypassIssuer(httpMate);
    }

    @Override
    public <T> T issue(final HttpClientRequest<T> request,
                       final Function<RawClientResponse, T> responseMapper) {
        final MetaData metaData = emptyMetaData();
        final RequestPath requestPath = request.path();
        metaData.set(RAW_PATH, requestPath.path());
        final Map<String, String> queryParameters = requestPath.queryParameters()
                .stream()
                .collect(toMap(
                        queryParameter -> queryParameter.key().encoded(),
                        queryParameter -> queryParameter.value().map(UriString::encoded).orElse(""))
                );
        metaData.set(RAW_REQUEST_QUERY_PARAMETERS, queryParameters);
        metaData.set(RAW_METHOD, request.method());
        metaData.set(RAW_REQUEST_HEADERS, mapToMultiMap(request.headers()));
        request.body().ifPresent(inputStream -> metaData.set(REQUEST_BODY_STREAM, inputStream));
        metaData.set(IS_HTTP_REQUEST, true);

        final SynchronizationWrapper<MetaData> wrapper = new SynchronizationWrapper<>();
        this.httpMate.handleRequest(metaData, wrapper::setObject);

        final Map<String, String> responseHeaders = metaData.get(RESPONSE_HEADERS);
        final int responseStatus = metaData.get(RESPONSE_STATUS);
        final InputStream responseBody = metaData.get(RESPONSE_STREAM);

        final RawClientResponse response = rawClientResponse(responseStatus, responseHeaders, responseBody);
        return responseMapper.apply(response);
    }
}
