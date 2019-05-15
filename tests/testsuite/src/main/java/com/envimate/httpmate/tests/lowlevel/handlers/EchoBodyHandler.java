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

package com.envimate.httpmate.tests.lowlevel.handlers;

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.handler.Handler;

import static com.envimate.httpmate.HttpMateChainKeys.BODY_STRING;
import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_STRING;

public final class EchoBodyHandler implements Handler {

    public static EchoBodyHandler echoBodyHandler() {
        return new EchoBodyHandler();
    }

    @Override
    public void handle(final MetaData metaData) {
        final String responseBody = metaData.getOptional(BODY_STRING).orElse("");
        metaData.set(RESPONSE_STRING, responseBody);
    }
}
