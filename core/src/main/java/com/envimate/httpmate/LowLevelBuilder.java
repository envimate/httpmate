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
import com.envimate.httpmate.chains.Configurator;
import com.envimate.httpmate.convenience.handler.HttpHandler;
import com.envimate.httpmate.convenience.handler.HttpRequest;
import com.envimate.httpmate.convenience.handler.HttpResponse;
import com.envimate.httpmate.generator.builder.ConditionStage;
import com.envimate.httpmate.handler.Handler;
import com.envimate.httpmate.http.HttpRequestMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static com.envimate.httpmate.CoreModule.coreModule;
import static com.envimate.httpmate.HttpMateBuilder.httpMateBuilder;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LowLevelBuilder {
    public static final HttpMateConfigurationType<LowLevelBuilder> LOW_LEVEL = LowLevelBuilder::lowLevelBuilder;

    private final CoreModule coreModule;
    private final List<Configurator> configurators = new LinkedList<>();
    private final List<ChainModule> modules = new LinkedList<>();

    public static LowLevelBuilder lowLevelBuilder() {
        return new LowLevelBuilder(coreModule());
    }

    public LowLevelBuilder get(final String url, final BiConsumer<HttpRequest, HttpResponse> handler) {
        return this
                .callingTheHandler((HttpHandler) handler::accept)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.GET);
    }

    public LowLevelBuilder post(final String url, final BiConsumer<HttpRequest, HttpResponse> handler) {
        return this
                .callingTheHandler((HttpHandler) handler::accept)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.POST);
    }

    public LowLevelBuilder put(final String url, final BiConsumer<HttpRequest, HttpResponse> handler) {
        return this
                .callingTheHandler((HttpHandler) handler::accept)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.PUT);
    }

    public LowLevelBuilder delete(final String url, final BiConsumer<HttpRequest, HttpResponse> handler) {
        return this
                .callingTheHandler((HttpHandler) handler::accept)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.DELETE);
    }

    public ConditionStage<LowLevelBuilder> callingTheHandler(final Handler handler) {
        return condition -> {
            coreModule.addHandler(handler, condition);
            return this;
        };
    }

    public HttpMateBuilder thatIs() {
        return httpMateBuilder(coreModule);
    }
}
