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

package com.envimate.httpmate.processors;

import com.envimate.httpmate.chains.rules.Processor;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.convenience.Http;
import com.envimate.httpmate.request.ContentType;
import com.envimate.httpmate.request.Headers;
import com.envimate.httpmate.request.QueryParameters;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static com.envimate.httpmate.chains.HttpMateChainKeys.*;
import static com.envimate.httpmate.request.ContentType.fromString;
import static com.envimate.httpmate.request.Headers.headers;
import static com.envimate.httpmate.request.HttpRequestMethod.parse;
import static com.envimate.httpmate.request.QueryParameters.queryParameters;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TranslateToValueObjectsProcessor implements Processor {

    public static Processor translateToValueObjectsProcessor() {
        return new TranslateToValueObjectsProcessor();
    }

    @Override
    public void apply(final MetaData metaData) {
        final Map<String, String> rawHeaders = metaData.get(RAW_HEADERS);
        final Headers headers = headers(rawHeaders);
        metaData.set(HEADERS, headers);
        final ContentType contentType = fromString(headers.getHeader(Http.Headers.CONTENT_TYPE));
        metaData.set(CONTENT_TYPE, contentType);

        final Map<String, String> rawQueryParameters = metaData.get(RAW_QUERY_PARAMETERS);
        final QueryParameters queryParameters = queryParameters(rawQueryParameters);
        metaData.set(QUERY_PARAMETERS, queryParameters);

        final String rawMethod = metaData.get(RAW_METHOD);
        metaData.set(METHOD, parse(rawMethod));
    }
}
