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

import com.envimate.httpmate.chains.*;
import com.envimate.httpmate.generator.builder.ConditionStage;
import com.envimate.httpmate.handler.Handler;
import com.envimate.httpmate.handler.http.HttpHandler;
import com.envimate.httpmate.http.HttpRequestMethod;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.envimate.httpmate.CoreModule.coreModule;
import static com.envimate.httpmate.HttpMate.httpMate;
import static com.envimate.httpmate.chains.ChainRegistryBuilder.chainRegistryBuilder;
import static com.envimate.httpmate.http.HttpRequestMethod.*;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMateBuilder {
    private boolean autodetectionOfModules = true;
    private final CoreModule coreModule;
    private final List<Configurator> configurators;

    static HttpMateBuilder httpMateBuilder() {
        return new HttpMateBuilder(coreModule(), new LinkedList<>());
    }

    public HttpMateBuilder disableAutodectectionOfModules() {
        autodetectionOfModules = false;
        return this;
    }

    public HttpMateBuilder get(final String url, final Object handler) {
        return this
                .serving(handler)
                .forRequestPath(url)
                .andRequestMethod(GET);
    }

    public HttpMateBuilder get(final String url, final HttpHandler handler) {
        return get(url, (Object) handler);
    }

    public HttpMateBuilder get(final String url, final Processor handler) {
        return get(url, (Handler) handler::apply);
    }

    public HttpMateBuilder post(final String url, final Object handler) {
        return this
                .serving(handler)
                .forRequestPath(url)
                .andRequestMethod(POST);
    }

    public HttpMateBuilder post(final String url, final HttpHandler handler) {
        return post(url, (Object) handler);
    }

    public HttpMateBuilder post(final String url, final Consumer<MetaData> handler) {
        return post(url, (Handler) handler::accept);
    }

    public HttpMateBuilder put(final String url, final Object handler) {
        return this
                .serving(handler)
                .forRequestPath(url)
                .andRequestMethod(PUT);
    }

    public HttpMateBuilder put(final String url, final HttpHandler handler) {
        return put(url, (Object) handler);
    }

    public HttpMateBuilder put(final String url, final Consumer<MetaData> handler) {
        return put(url, (Handler) handler::accept);
    }

    public HttpMateBuilder delete(final String url, final Object handler) {
        return this
                .serving(handler)
                .forRequestPath(url)
                .andRequestMethod(HttpRequestMethod.DELETE);
    }

    public HttpMateBuilder delete(final String url, final HttpHandler handler) {
        return delete(url, (Object) handler);
    }

    public HttpMateBuilder delete(final String url, final Consumer<MetaData> handler) {
        return delete(url, (Handler) handler::accept);
    }

    public ConditionStage<HttpMateBuilder> serving(final Object handler) {
        validateNotNull(handler, "handler");
        return condition -> {
            coreModule.registerHandler(condition, handler);
            return this;
        };
    }

    public ConditionStage<HttpMateBuilder> serving(final Handler handler) {
        return serving((Object) handler);
    }

    public HttpMateBuilder configured(final ConfiguratorBuilder configuratorBuilder) {
        validateNotNull(configuratorBuilder, "configuratorBuilder");
        final Configurator configurator = configuratorBuilder.build();
        return configured(configurator);
    }

    public HttpMateBuilder configured(final Configurator configurator) {
        validateNotNull(configurator, "configurator");
        configurators.add(configurator);
        return this;
    }

    public HttpMate build() {
        final ChainRegistryBuilder chainRegistryBuilder = chainRegistryBuilder();
        chainRegistryBuilder.addModule(coreModule);
        if(autodetectionOfModules) {
            chainRegistryBuilder.addModuleIfPresent("com.envimate.httpmate.events.EventModule");
            chainRegistryBuilder.addModuleIfPresent("com.envimate.httpmate.usecases.UseCasesModule");
        }
        configurators.forEach(chainRegistryBuilder::addConfigurator);
        final ChainRegistry chainRegistry = chainRegistryBuilder.build();
        return httpMate(chainRegistry);
    }
}

