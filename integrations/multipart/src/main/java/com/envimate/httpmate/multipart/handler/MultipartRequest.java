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

package com.envimate.httpmate.multipart.handler;

import com.envimate.httpmate.handler.http.HttpRequest;
import com.envimate.httpmate.http.Headers;
import com.envimate.httpmate.http.PathParameters;
import com.envimate.httpmate.http.QueryParameters;
import com.envimate.httpmate.multipart.MultipartIteratorBody;
import com.envimate.httpmate.path.Path;
import lombok.*;

import static com.envimate.httpmate.multipart.MultipartChainKeys.MULTIPART_ITERATOR_BODY;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipartRequest {
    private final HttpRequest request;

    static MultipartRequest multipartRequest(final HttpRequest request) {
        validateNotNull(request, "request");
        return new MultipartRequest(request);
    }

    public Path path() {
        return request.path();
    }

    public PathParameters pathParameters() {
        return request.pathParameters();
    }

    public QueryParameters queryParameters() {
        return request.queryParameters();
    }

    public Headers headers() {
        return request.headers();
    }

    public MultipartIteratorBody partIterator() {
        return request.getMetaData().get(MULTIPART_ITERATOR_BODY);
    }
}
