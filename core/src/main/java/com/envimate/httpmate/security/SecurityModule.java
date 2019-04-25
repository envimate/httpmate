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

import com.envimate.httpmate.chains.ChainExtender;
import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.chains.Processor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityModule implements ChainModule {
    private final Map<ChainName, List<Authenticator>> authenticators = new HashMap<>();
    private final Map<ChainName, List<Authorizer>> authorizers = new HashMap<>();
    private final Map<ChainName, List<Processor>> filters = new HashMap<>();

    public static ChainModule securityModule() {
        return new SecurityModule();
    }

    public void addAuthenticator(final ChainName chainName,
                                 final Authenticator authenticator) {
        validateNotNull(chainName, "chainName");
        validateNotNull(authenticator, "authenticator");
        add(chainName, authenticator, authenticators);
    }

    public void addAuthorizer(final ChainName chainName,
                              final Authorizer authorizer) {
        validateNotNull(chainName, "chainName");
        validateNotNull(authorizer, "authorizer");
        add(chainName, authorizer, authorizers);
    }

    public void addFilter(final ChainName chainName,
                          final Processor filter) {
        validateNotNull(chainName, "chainName");
        validateNotNull(filter, "filter");
        add(chainName, filter, filters);
    }

    private static <T extends Processor> void add(final ChainName chainName,
                                                  final T element,
                                                  final Map<ChainName, List<T>> multiMap) {
        final List<T> forChain = multiMap.computeIfAbsent(chainName, x -> new LinkedList<>());
        forChain.add(element);
    }

    @Override
    public void register(final ChainExtender extender) {
        authenticators.forEach((chainName, authenticatorsForChain) ->
                authenticatorsForChain.forEach(authenticator -> extender.addProcessor(chainName, authenticator)));
        authorizers.forEach((chainName, authorizersForChain) ->
                authorizersForChain.forEach(authorizer -> extender.addProcessor(chainName, authorizer)));
        filters.forEach((chainName, filtersForChain) ->
                filtersForChain.forEach(filter -> extender.addProcessor(chainName, filter)));
    }
}
