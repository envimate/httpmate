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

package com.envimate.httpmate;

import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.chains.ChainRegistry;
import com.envimate.httpmate.chains.ChainRegistryBuilder;
import com.envimate.httpmate.chains.Configurator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.httpmate.HttpMate.httpMate;
import static com.envimate.httpmate.chains.ChainRegistryBuilder.chainRegistryBuilder;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Arrays.asList;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMateBuilder {
    private final List<ChainModule> modules;
    private final List<Configurator> configurators;

    public static HttpMateBuilder httpMateBuilder(final ChainModule... initialModules) {
        validateNotNull(initialModules, "initialModules");
        return new HttpMateBuilder(new LinkedList<>(asList(initialModules)), new LinkedList<>());
    }

    public HttpMateBuilder configured(final Configurator configurator) {
        validateNotNull(configurator, "configurator");
        configurators.add(configurator);
        return this;
    }

    public HttpMate build() {
        final ChainRegistryBuilder chainRegistryBuilder = chainRegistryBuilder();
        modules.forEach(chainRegistryBuilder::addModule);
        configurators.forEach(chainRegistryBuilder::addConfigurator);
        final ChainRegistry chainRegistry = chainRegistryBuilder.build();
        return httpMate(chainRegistry);
    }
}

