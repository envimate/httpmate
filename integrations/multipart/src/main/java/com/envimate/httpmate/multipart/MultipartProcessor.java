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

package com.envimate.httpmate.multipart;

import com.envimate.httpmate.chains.Processor;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.http.ContentType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.InputStream;

import static com.envimate.httpmate.HttpMateChainKeys.CONTENT_TYPE;
import static com.envimate.httpmate.HttpMateChainKeys.BODY_STREAM;
import static com.envimate.httpmate.multipart.MultipartChainKeys.MULTIPART_ITERATOR_BODY;
import static com.envimate.httpmate.multipart.MultipartHandler.parse;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipartProcessor implements Processor {

    public static Processor multipartProcessor() {
        return new MultipartProcessor();
    }

    @Override
    public void apply(final MetaData metaData) {
        final InputStream body = metaData.get(BODY_STREAM);
        final ContentType contentType = metaData.get(CONTENT_TYPE);
        final MultipartIteratorBody multipartIteratorBody = parse(body, contentType);
        metaData.set(MULTIPART_ITERATOR_BODY, multipartIteratorBody);
    }
}
