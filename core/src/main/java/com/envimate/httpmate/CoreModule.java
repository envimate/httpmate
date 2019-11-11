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

import com.envimate.httpmate.chains.ChainExtender;
import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.chains.DependencyRegistry;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.closing.ClosingAction;
import com.envimate.httpmate.closing.ClosingActions;
import com.envimate.httpmate.exceptions.ExceptionMapper;
import com.envimate.httpmate.exceptions.ExceptionSerializer;
import com.envimate.httpmate.filtermap.FilterMapBuilder;
import com.envimate.httpmate.generator.GenerationCondition;
import com.envimate.httpmate.generator.Generator;
import com.envimate.httpmate.handler.Handler;
import com.envimate.httpmate.handler.distribution.HandlerDistributors;
import com.envimate.httpmate.logger.LoggerImplementation;
import com.envimate.httpmate.responsetemplate.ResponseTemplate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.envimate.httpmate.HttpMateChainKeys.RESPONSE_STATUS;
import static com.envimate.httpmate.HttpMateChains.*;
import static com.envimate.httpmate.backchannel.BackChannelFactory.BACK_CHANNEL_FACTORY;
import static com.envimate.httpmate.backchannel.LocalBackChannelFactory.localBackChannelFactory;
import static com.envimate.httpmate.chains.builder.ChainBuilder.extendAChainWith;
import static com.envimate.httpmate.chains.rules.Consume.consume;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.closing.ClosingActions.CLOSING_ACTIONS;
import static com.envimate.httpmate.closing.ClosingActions.closingActions;
import static com.envimate.httpmate.exceptions.DefaultExceptionMapper.theDefaultExceptionMapper;
import static com.envimate.httpmate.exceptions.ExceptionSerializer.exceptionSerializer;
import static com.envimate.httpmate.filtermap.FilterMapBuilder.filterMapBuilder;
import static com.envimate.httpmate.generator.Generator.generator;
import static com.envimate.httpmate.generator.Generators.generators;
import static com.envimate.httpmate.handler.DetermineHandlerProcessor.determineHandlerProcessor;
import static com.envimate.httpmate.handler.InvokeHandlerProcessor.invokeHandlerProcessor;
import static com.envimate.httpmate.handler.distribution.HandlerDistributors.HANDLER_DISTRIBUTORS;
import static com.envimate.httpmate.handler.distribution.HandlerDistributors.handlerDistributors;
import static com.envimate.httpmate.http.Http.StatusCodes.INTERNAL_SERVER_ERROR;
import static com.envimate.httpmate.logger.Loggers.stdoutAndStderrLogger;
import static com.envimate.httpmate.logger.SetLoggerProcessor.setLoggerProcessor;
import static com.envimate.httpmate.processors.MapExceptionProcessor.mapExceptionProcessor;
import static com.envimate.httpmate.processors.StreamToStringProcessor.streamToStringProcessor;
import static com.envimate.httpmate.processors.StringBodyToStreamProcessor.stringBodyToStreamProcessor;
import static com.envimate.httpmate.processors.TranslateToValueObjectsProcessor.translateToValueObjectsProcessor;
import static com.envimate.httpmate.responsetemplate.ApplyResponseTemplateProcessor.applyResponseTemplateProcessor;
import static com.envimate.httpmate.responsetemplate.InitResponseProcessor.initResponseProcessor;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoreModule implements ChainModule {
    private final Map<GenerationCondition, Object> handlers = new HashMap<>();
    private final List<Generator<Handler>> lowLevelHandlers = new LinkedList<>();
    private ResponseTemplate responseTemplate = ResponseTemplate.EMPTY_RESPONSE_TEMPLATE;
    private final FilterMapBuilder<Throwable, ExceptionMapper<Throwable>> exceptionMappers = filterMapBuilder();
    private LoggerImplementation logger = stdoutAndStderrLogger();
    private final ClosingActions closingActions = closingActions();

    public static CoreModule coreModule() {
        final CoreModule coreModule = new CoreModule();
        coreModule.setDefaultExceptionMapper(theDefaultExceptionMapper());
        return coreModule;
    }

    public void registerHandler(final GenerationCondition condition,
                                final Object handler) {
        validateNotNull(condition, "generationCondition");
        validateNotNull(handler, "handler");
        handlers.put(condition, handler);
    }

    public void setLogger(final LoggerImplementation logger) {
        validateNotNull(logger, "logger");
        this.logger = logger;
    }

    public void addClosingAction(final ClosingAction closingAction) {
        validateNotNull(closingAction, "closingAction");
        closingActions.addClosingAction(closingAction);
    }

    public void setResponseTemplate(final ResponseTemplate responseTemplate) {
        validateNotNull(responseTemplate, "responseTemplate");
        this.responseTemplate = responseTemplate;
    }

    public void addExceptionMapper(final Predicate<Throwable> filter,
                                   final ExceptionMapper<Throwable> responseMapper) {
        validateNotNull(filter, "filter");
        validateNotNull(responseMapper, "responseMapper");
        this.exceptionMappers.put(filter, responseMapper);
    }

    public void setDefaultExceptionMapper(final ExceptionMapper<Throwable> responseMapper) {
        validateNotNull(responseMapper, "responseMapper");
        this.exceptionMappers.setDefaultValue(responseMapper);
    }

    @Override
    public void init(final MetaData configurationMetaData) {
        final HandlerDistributors handlerDistributers = handlerDistributors();
        configurationMetaData.set(HANDLER_DISTRIBUTORS, handlerDistributers);
        handlerDistributers.register(handler -> handler instanceof Handler, (handler, condition) -> {
            final Generator<Handler> generator = generator((Handler) handler, condition);
            lowLevelHandlers.add(generator);
        });
    }

    @Override
    public void configure(final DependencyRegistry dependencyRegistry) {
        final HandlerDistributors handlerDistributors = dependencyRegistry.getMetaDatum(HANDLER_DISTRIBUTORS);
        handlers.forEach((condition, handler) -> handlerDistributors.distribute(handler, condition));
    }

    @Override
    public void register(final ChainExtender extender) {
        final ExceptionSerializer exceptionSerializer = exceptionSerializer(exceptionMappers.build());
        extendAChainWith(extender)
                .append(INIT, setLoggerProcessor(logger))
                .append(PRE_PROCESS, translateToValueObjectsProcessor())
                .append(PROCESS_HEADERS)
                .append(PROCESS_BODY)
                .append(PROCESS_BODY_STRING, streamToStringProcessor())
                .append(DETERMINE_HANDLER, determineHandlerProcessor(generators(lowLevelHandlers)))
                .append(PREPARE_RESPONSE, initResponseProcessor(), applyResponseTemplateProcessor(responseTemplate))
                .append(INVOKE_HANDLER, invokeHandlerProcessor())
                .append(POST_INVOKE)
                .withTheExceptionChain(EXCEPTION_OCCURRED)
                .withTheFinalAction(jumpTo(POST_PROCESS));

        extendAChainWith(extender)
                .append(EXCEPTION_OCCURRED)
                .append(PREPARE_EXCEPTION_RESPONSE,
                        initResponseProcessor(),
                        metaData -> metaData.set(RESPONSE_STATUS, INTERNAL_SERVER_ERROR))
                .append(MAP_EXCEPTION_TO_RESPONSE, mapExceptionProcessor(exceptionSerializer))
                .withTheExceptionChain(ERROR)
                .withTheFinalAction(jumpTo(POST_INVOKE));

        extender.createChain(POST_PROCESS, consume(), jumpTo(ERROR));
        extender.appendProcessor(POST_PROCESS, stringBodyToStreamProcessor());

        extender.createChain(ERROR, consume(), consume());

        extender.addMetaDatum(CLOSING_ACTIONS, closingActions);
        extender.addMetaDatum(BACK_CHANNEL_FACTORY, localBackChannelFactory());
    }
}
