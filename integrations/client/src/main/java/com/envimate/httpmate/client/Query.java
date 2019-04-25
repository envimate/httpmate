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

package com.envimate.httpmate.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.envimate.httpmate.client.QueryParameterKey.queryParameterKey;
import static com.envimate.httpmate.client.QueryParameterValue.queryParameterValue;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class Query {
    private final String path;
    private final Map<QueryParameterKey, QueryParameterValue> encodedQueryParameters;

    static Query parse(final String query) {
        final String[] pathAndQueryParameters = query.split("\\?");
        final String path = pathAndQueryParameters[0];
        final Map<QueryParameterKey, QueryParameterValue> encodedQueryParameters = new HashMap<>();
        if(pathAndQueryParameters.length > 1) {
            final String queryParamters = pathAndQueryParameters[1];
            final String[] queryParametersArray = queryParamters.split("&");
            Arrays.stream(queryParametersArray)
                    .forEach(element -> {
                        final String[] keyValue = element.split("=");
                        final QueryParameterKey key = queryParameterKey(keyValue[0]);
                        final QueryParameterValue value;
                        if(keyValue.length > 1) {
                            value = queryParameterValue(keyValue[1]);
                        } else {
                            value = queryParameterValue("");
                        }
                        encodedQueryParameters.put(key, value);
                    });
        }
        return new Query(path, encodedQueryParameters);
    }

    String path() {
        return path;
    }

    Map<QueryParameterKey, QueryParameterValue> encodedQueryParameters() {
        return encodedQueryParameters;
    }
}
