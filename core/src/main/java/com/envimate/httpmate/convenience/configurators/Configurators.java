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

package com.envimate.httpmate.convenience.configurators;

import com.envimate.httpmate.CoreModule;
import com.envimate.httpmate.chains.Configurator;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.convenience.handler.HttpHandler;
import com.envimate.httpmate.generator.GenerationCondition;
import com.envimate.httpmate.handler.Handler;
import com.envimate.httpmate.http.HttpRequestMethod;
import com.envimate.httpmate.logger.Logger;
import com.envimate.httpmate.responsetemplate.ResponseTemplate;

import java.util.function.Predicate;

import static com.envimate.httpmate.chains.Configurator.configuratorForType;
import static com.envimate.httpmate.generator.PathAndMethodGenerationCondition.pathAndMethodEventTypeGenerationCondition;
import static com.envimate.httpmate.http.HttpRequestMethod.*;
import static com.envimate.httpmate.path.PathTemplate.pathTemplate;
import static com.envimate.httpmate.util.Validators.validateNotNull;

public final class Configurators {

    private Configurators() {
    }

    public static Configurator toHandleGetRequestsTo(final String route, final HttpHandler handler) {
        return toHandleHttpRequestsTo(route, GET, handler);
    }

    public static Configurator toHandlePostRequestsTo(final String route, final HttpHandler handler) {
        return toHandleHttpRequestsTo(route, POST, handler);
    }

    public static Configurator toHandlePutRequestsTo(final String route, final HttpHandler handler) {
        return toHandleHttpRequestsTo(route, PUT, handler);
    }

    public static Configurator toHandleDeleteRequestsTo(final String route, final HttpHandler handler) {
        return toHandleHttpRequestsTo(route, DELETE, handler);
    }

    public static Configurator toHandleHttpRequestsTo(final String route, final HttpRequestMethod method, final HttpHandler handler) {
        validateNotNull(route, "route");
        validateNotNull(method, "method");
        validateNotNull(handler, "handler");
        final GenerationCondition generationCondition = pathAndMethodEventTypeGenerationCondition(pathTemplate(route), method);
        return toHandleRequests(generationCondition::generate, handler);
    }

    public static Configurator toHandleRequests(final Predicate<MetaData> predicate, final Handler handler) {
        validateNotNull(predicate, "predicate");
        validateNotNull(handler, "handler");
        return configuratorForType(CoreModule.class, coreModule -> coreModule.addHandler(handler, predicate::test));
    }

    public static Configurator toCustomizeResponsesUsing(final ResponseTemplate responseTemplate) {
        validateNotNull(responseTemplate, "responseTemplate");
        return dependencyRegistry -> {
            final CoreModule coreModule = dependencyRegistry.getDependency(CoreModule.class);
            coreModule.setResponseTemplate(responseTemplate);
        };
    }

    public static Configurator toLogUsing(final Logger logger) {
        validateNotNull(logger, "logger");
        return dependencyRegistry -> {
            final CoreModule coreModule = dependencyRegistry.getDependency(CoreModule.class);
            coreModule.setLogger(logger);
        };
    }
}
