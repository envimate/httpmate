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

package com.envimate.httpmate.client.requestbuilder;

import com.envimate.httpmate.client.requestbuilder.multipart.Part;

import java.io.InputStream;

import static com.envimate.httpmate.client.requestbuilder.Body.bodyWithoutContentType;
import static com.envimate.httpmate.client.requestbuilder.multipart.MultipartBodyCreator.createMultipartBody;
import static com.envimate.httpmate.util.Streams.stringToInputStream;

public interface BodyStage {
    default HeadersAndQueryParametersAndMappingStage withoutABody() {
        return withTheBody("");
    }

    default HeadersAndQueryParametersAndMappingStage withAMultipartBodyWithTheParts(final Part... parts) {
        return withTheBody(createMultipartBody(parts));
    }

    default HeadersAndQueryParametersAndMappingStage withTheBody(final String body) {
        return withTheBody(stringToInputStream(body));
    }

    default HeadersAndQueryParametersAndMappingStage withTheBody(final InputStream body) {
        return withTheBody(bodyWithoutContentType(() -> body));
    }

    HeadersAndQueryParametersAndMappingStage withTheBody(Body body);
}
