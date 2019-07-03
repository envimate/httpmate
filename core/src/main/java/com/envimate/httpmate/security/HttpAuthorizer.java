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

package com.envimate.httpmate.security;

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.convenience.handler.HttpRequest;

import java.util.Optional;

import static com.envimate.httpmate.HttpMateChainKeys.AUTHENTICATION_INFORMATION;
import static com.envimate.httpmate.convenience.handler.HttpRequest.httpRequest;

public interface HttpAuthorizer<T> extends Authorizer {

    @SuppressWarnings("unchecked")
    @Override
    default boolean isAuthorized(final MetaData metaData) {
        final Optional<T> authenticationInformation = (Optional<T>) metaData.getOptional(AUTHENTICATION_INFORMATION);
        final HttpRequest request = httpRequest(metaData);
        return isAuthorized(authenticationInformation, request);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    boolean isAuthorized(Optional<T> authenticationInformation, HttpRequest request);
}
