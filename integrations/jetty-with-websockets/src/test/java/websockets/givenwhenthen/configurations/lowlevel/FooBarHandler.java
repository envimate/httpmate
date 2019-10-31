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

package websockets.givenwhenthen.configurations.lowlevel;

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.handler.Handler;
import com.envimate.httpmate.http.Headers;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.HttpMateChainKeys.REQUEST_HEADERS;
import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_BODY_STRING;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FooBarHandler implements Handler {

    public static Handler fooBarHandler() {
        return new FooBarHandler();
    }

    @Override
    public void handle(final MetaData metaData) {
        final Headers headers = metaData.get(REQUEST_HEADERS);
        final String foo = headers.getOptionalHeader("foo").orElseThrow();
        metaData.set(RESPONSE_BODY_STRING, foo);
    }
}
