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

package com.envimate.httpmate;

import com.envimate.httpmate.builder.Using;
import com.envimate.httpmate.builder.configurators.ExceptionConfigurator;
import com.envimate.httpmate.builder.configurators.LoggerConfigurator;
import com.envimate.httpmate.builder.configurators.ResponseTemplateConfigurator;
import com.envimate.httpmate.builder.configurators.SecurityConfigurator;
import com.envimate.httpmate.chains.ChainRegistry;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.chains.rules.Processor;
import com.envimate.httpmate.convenience.preprocessors.Authenticator;
import com.envimate.httpmate.convenience.preprocessors.Authorizer;
import com.envimate.httpmate.event.EventTypeGenerationCondition;
import com.envimate.httpmate.event.EventTypeGenerator;
import com.envimate.httpmate.event.EventTypeGenerators;
import com.envimate.httpmate.mapper.*;
import com.envimate.httpmate.mapper.filtermap.FilterMapBuilder;
import com.envimate.httpmate.request.HttpRequestMethod;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static com.envimate.httpmate.Core.setupRegistry;
import static com.envimate.httpmate.HttpMate.httpMate;
import static com.envimate.httpmate.HttpMateDefaults.setSecureDefaults;
import static com.envimate.httpmate.convenience.event.PathAndMethodEventTypeGenerationCondition.pathAndMethodEventTypeGenerationCondition;
import static com.envimate.httpmate.event.EventTypeGenerator.eventTypeGenerator;
import static com.envimate.httpmate.event.EventTypeGenerators.eventTypeGenerators;
import static com.envimate.httpmate.mapper.ExceptionSerializer.responseSerializer;
import static com.envimate.httpmate.mapper.filtermap.FilterMapBuilder.filterMapBuilder;
import static com.envimate.httpmate.path.PathTemplate.pathTemplate;
import static com.envimate.httpmate.util.Validators.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpMateConfigurator {
    private MessageBus messageBus;
    private final List<EventTypeGenerator> eventTypeGenerators = new LinkedList<>();
    private final List<Authenticator> authenticators = new LinkedList<>();
    private final List<Authorizer> authorizers = new LinkedList<>();
    private final FilterMapBuilder<MetaData, RequestToEventMapper> requestToEventMappers = filterMapBuilder();
    private ResponseTemplate responseTemplate;
    private EventToResponseMapper responseMapper;
    private final FilterMapBuilder<Throwable, ResponseMapper<Throwable>> exceptionMappers = filterMapBuilder();
    private Logger logger;
    private final List<Module> modules = new LinkedList<>();
    private final List<Processor> requestFilters = new LinkedList<>();

    static HttpMateConfigurator httpMateConfigurator() {
        final HttpMateConfigurator httpMateBuilder = new HttpMateConfigurator();
        setSecureDefaults(httpMateBuilder);
        return httpMateBuilder;
    }

    HttpMate build(final MessageBus messageBus) {
        modules.forEach(module -> module.configure(this));
        final EventTypeGenerators eventTypeGenerators = eventTypeGenerators(this.eventTypeGenerators);
        final ExceptionSerializer responseSerializer = responseSerializer(exceptionMappers.build());
        final ChainRegistry chainRegistry = setupRegistry(
                messageBus,
                responseSerializer,
                responseMapper,
                responseTemplate,
                eventTypeGenerators,
                requestToEventMappers.build(),
                authenticators,
                authorizers,
                requestFilters);
        modules.forEach(module -> module.register(chainRegistry, messageBus));
        return httpMate(chainRegistry, messageBus, logger);
    }

    public void setMessageBus(final MessageBus messageBus) {
        validateNotNull(messageBus, "messageBus");
        this.messageBus = messageBus;
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }

    public void filterRequests(final Processor filter) {
        validateNotNull(filter, "filter");
        requestFilters.add(filter);
    }

    public void addRequestToEventMapper(final Predicate<MetaData> filter,
                                        final RequestToEventMapper mapper) {
        validateNotNull(filter, "filter");
        validateNotNull(mapper, "mapper");
        requestToEventMappers.put(filter, mapper);
    }

    public void setResponseMapper(final EventToResponseMapper responseMapper) {
        validateNotNull(responseMapper, "responseMapper");
        this.responseMapper = responseMapper;
    }

    public void setDefaultRequestToEventMapper(final RequestToEventMapper requestToEventMapper) {
        validateNotNull(requestToEventMapper, "requestToEventMapper");
        requestToEventMappers.setDefaultValue(requestToEventMapper);
    }

    void addEventMapping(final EventType eventType,
                         final String requestPath,
                         final HttpRequestMethod... methods) {
        validateNotNull(eventType, "eventType");
        validateNotNullNorEmpty(requestPath, "requestPath");
        validateArrayNeitherNullNorEmptyNorContainsNull(methods, "methods");
        final EventTypeGenerationCondition eventTypeGenerationCondition =
                pathAndMethodEventTypeGenerationCondition(pathTemplate(requestPath), methods);
        final EventTypeGenerator eventTypeGenerator = eventTypeGenerator(eventType, eventTypeGenerationCondition);
        eventTypeGenerators.add(eventTypeGenerator);
    }

    private void addAuthenticator(final Authenticator authenticator) {
        validateNotNull(authenticator, "authenticator");
        this.authenticators.add(authenticator);
    }

    private void addAuthorizer(final Authorizer authorizer) {
        validateNotNull(authorizer, "authorizer");
        this.authorizers.add(authorizer);
    }

    void setResponseTemplate(final ResponseTemplate responseTemplate) {
        validateNotNull(responseTemplate, "responseTemplate");
        this.responseTemplate = responseTemplate;
    }

    void addExceptionMapper(final Predicate<Throwable> filter,
                            final ResponseMapper<Throwable> responseMapper) {
        validateNotNull(filter, "filter");
        validateNotNull(responseMapper, "responseMapper");
        this.exceptionMappers.put(filter, responseMapper);
    }

    void setDefaultExceptionMapper(final ResponseMapper<Throwable> responseMapper) {
        validateNotNull(responseMapper, "responseMapper");
        this.exceptionMappers.setDefaultValue(responseMapper);
    }

    public SecurityConfigurator configureSecurity() {
        return new SecurityConfigurator() {
            @Override
            public void addAuthenticator(final Authenticator authenticator) {
                HttpMateConfigurator.this.addAuthenticator(authenticator);
            }

            @Override
            public void addAuthorizer(final Authorizer authorizer) {
                HttpMateConfigurator.this.addAuthorizer(authorizer);
            }
        };
    }

    public ExceptionConfigurator configureExceptionMapping() {
        return new ExceptionConfigurator() {
            @Override
            public Using<HttpMateConfigurator, ResponseMapper<Throwable>> mappingExceptionsThat(
                    final Predicate<Throwable> filter) {
                return responseMapper -> {
                    addExceptionMapper(filter, responseMapper);
                    return HttpMateConfigurator.this;
                };
            }

            @Override
            public void mappingExceptionsByDefaultUsing(final ResponseMapper<Throwable> mapper) {
                setDefaultExceptionMapper(mapper);
            }
        };
    }

    public ResponseTemplateConfigurator configureResponseTemplate() {
        return this::setResponseTemplate;
    }

    public LoggerConfigurator configureLogger() {
        return logger -> {
            validateNotNull(logger, "logger");
            this.logger = logger;
        };
    }

    public void registerModule(final Module module) {
        validateNotNull(module, "module");
        modules.add(module);
    }
}
