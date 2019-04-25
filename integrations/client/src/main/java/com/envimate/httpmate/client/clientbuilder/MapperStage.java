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

package com.envimate.httpmate.client.clientbuilder;

import com.envimate.httpmate.client.ClientResponseMapper;
import com.envimate.httpmate.client.HttpMateClient;
import com.envimate.httpmate.client.RawClientResponse;
import com.envimate.httpmate.client.SimpleHttpResponseObject;

import static com.envimate.httpmate.client.SimpleHttpResponseObject.httpClientResponse;
import static com.envimate.httpmate.client.UnsupportedTargetTypeException.unsupportedTargetTypeException;
import static com.envimate.httpmate.util.Streams.inputStreamToString;

public interface MapperStage {

    default HttpMateClient mappingToSimpleResponseObjects() {
        return mappingResponseObjectsUsing(new ClientResponseMapper() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T map(final RawClientResponse response, final Class<T> targetType) {
                if (targetType.equals(SimpleHttpResponseObject.class)) {
                    final String body = inputStreamToString(response.content());
                    return (T) httpClientResponse(response.statusCode(), response.headers(), body);
                }
                throw unsupportedTargetTypeException(SimpleHttpResponseObject.class, targetType);
            }
        });
    }

    default HttpMateClient mappingResponseObjectsToStrings() {
        return mappingResponseObjectsUsing(new ClientResponseMapper() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T map(final RawClientResponse response, final Class<T> targetType) {
                if (targetType.equals(String.class)) {
                    return (T) inputStreamToString(response.content());
                }
                throw unsupportedTargetTypeException(String.class, targetType);
            }
        });
    }

    HttpMateClient mappingResponseObjectsUsing(ClientResponseMapper responseMapper);
}
