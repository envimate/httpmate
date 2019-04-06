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

import com.envimate.httpmate.chains.Chain;
import com.envimate.httpmate.chains.ChainRegistry;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.chains.rules.Jump;
import com.envimate.httpmate.convenience.preprocessors.Authenticator;
import com.envimate.httpmate.convenience.preprocessors.Authorizer;
import com.envimate.httpmate.event.EventTypeGenerators;
import com.envimate.httpmate.mapper.EventToResponseMapper;
import com.envimate.httpmate.mapper.RequestToEventMapper;
import com.envimate.httpmate.mapper.ExceptionSerializer;
import com.envimate.httpmate.mapper.ResponseTemplate;
import com.envimate.httpmate.mapper.filtermap.FilterMap;
import com.envimate.messageMate.messageBus.MessageBus;

import java.util.List;

import static com.envimate.httpmate.chains.ChainRegistry.emptyChainRegistry;
import static com.envimate.httpmate.chains.HttpMateChains.*;
import static com.envimate.httpmate.chains.rules.Consume.consume;
import static com.envimate.httpmate.chains.rules.Drop.drop;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.processors.CreateHttpResponseBuilderProcessor.createHttpResponseBuilderProcessor;
import static com.envimate.httpmate.processors.DetermineEventProcessor.determineEventProcessor;
import static com.envimate.httpmate.processors.DispatchEventProcessor.dispatchEventProcessor;
import static com.envimate.httpmate.processors.MapExceptionProcessor.mapExceptionProcessor;
import static com.envimate.httpmate.processors.MapToEventProcessor.mapToEventProcessor;
import static com.envimate.httpmate.processors.SerializationProcessor.serializationProcessor;
import static com.envimate.httpmate.processors.StreamToStringProcessor.streamToStringProcessor;
import static com.envimate.httpmate.processors.StringBodyToStreamProcessor.stringBodyToStreamProcessor;
import static com.envimate.httpmate.processors.TranslateToValueObjectsProcessor.translateToValueObjectsProcessor;

final class Core {

    private Core() {
    }

    static ChainRegistry setupRegistry(final MessageBus messageBus,
                                       final ExceptionSerializer exceptionSerializer,
                                       final EventToResponseMapper eventToResponseMapper,
                                       final ResponseTemplate responseTemplate,
                                       final EventTypeGenerators useCaseGenerators,
                                       final FilterMap<MetaData, RequestToEventMapper> requestToEventMappers,
                                       final List<Authenticator> authenticators,
                                       final List<Authorizer> authorizers) {
        final ChainRegistry chainRegistry = createSkeleton();
        chainRegistry.addProcessorToChain(PRE_PROCESS, translateToValueObjectsProcessor());
        chainRegistry.addProcessorToChain(PROCESS_BODY_STRING, streamToStringProcessor());
        chainRegistry.addProcessorToChain(DETERMINE_EVENT, determineEventProcessor(useCaseGenerators));
        authenticators.forEach(processor -> chainRegistry.addProcessorToChain(AUTHENTICATION, processor));
        authorizers.forEach(processor -> chainRegistry.addProcessorToChain(AUTHORIZATION, processor));
        chainRegistry.addProcessorToChain(MAP_TO_EVENT, mapToEventProcessor(requestToEventMappers));
        chainRegistry.addProcessorToChain(SUBMIT, dispatchEventProcessor(messageBus));
        chainRegistry.addProcessorToChain(PRE_SERIALIZATION, createHttpResponseBuilderProcessor(responseTemplate));
        chainRegistry.addProcessorToChain(SERIALIZATION, serializationProcessor(eventToResponseMapper));
        chainRegistry.addProcessorToChain(POST_SERIALIZATION, stringBodyToStreamProcessor());
        chainRegistry.addProcessorToChain(EXCEPTION_OCCURRED, createHttpResponseBuilderProcessor(responseTemplate));
        chainRegistry.addProcessorToChain(EXCEPTION_OCCURRED, mapExceptionProcessor(exceptionSerializer));
        chainRegistry.addProcessorToChain(EXCEPTION_OCCURRED, stringBodyToStreamProcessor());
        return chainRegistry;
    }

    private static ChainRegistry createSkeleton() {
        final ChainRegistry chainRegistry = emptyChainRegistry();

        final Chain exceptionOccuredChain = chainRegistry.createChain(EXCEPTION_OCCURRED, consume(), drop());
        final Chain postSerializationChain = chainRegistry.createChain(
                POST_SERIALIZATION, consume(), jumpTo(exceptionOccuredChain));
        final Chain serializationChain = chainRegistry.createChain(
                SERIALIZATION, jumpTo(postSerializationChain), jumpTo(exceptionOccuredChain));
        final Chain preSerializationChain = chainRegistry.createChain(
                PRE_SERIALIZATION, jumpTo(serializationChain), jumpTo(exceptionOccuredChain));
        final Chain submitChain = chainRegistry.createChain(
                SUBMIT, jumpTo(preSerializationChain), jumpTo(exceptionOccuredChain));
        final Chain mapToEventChain = chainRegistry.createChain(
                MAP_TO_EVENT, jumpTo(submitChain), jumpTo(exceptionOccuredChain));
        final Chain preMapToEventChain = chainRegistry.createChain(
                PRE_MAP_TO_EVENT, jumpTo(mapToEventChain), jumpTo(exceptionOccuredChain));
        final Chain authorizationChain = chainRegistry.createChain(
                AUTHORIZATION, jumpTo(preMapToEventChain), jumpTo(exceptionOccuredChain));
        final Chain authenticationChain = chainRegistry.createChain(
                AUTHENTICATION, jumpTo(authorizationChain), jumpTo(exceptionOccuredChain));
        final Chain determineEventChain = chainRegistry.createChain(
                DETERMINE_EVENT, jumpTo(authenticationChain), jumpTo(exceptionOccuredChain));
        final Chain preDetermineEventChain = chainRegistry.createChain(
                PRE_DETERMINE_EVENT, jumpTo(determineEventChain), jumpTo(exceptionOccuredChain));
        final Chain processBodyStringChain = chainRegistry.createChain(
                PROCESS_BODY_STRING, jumpTo(preDetermineEventChain), jumpTo(exceptionOccuredChain));
        final Chain processSinglePartChain = chainRegistry.createChain(
                PROCESS_SINGLE_PART, jumpTo(processBodyStringChain), jumpTo(exceptionOccuredChain));
        final Chain processBodyChain = chainRegistry.createChain(
                PROCESS_BODY, jumpTo(processSinglePartChain), jumpTo(exceptionOccuredChain));
        final Chain processHeadersChain = chainRegistry.createChain(
                PROCESS_HEADERS, jumpTo(processBodyChain), Jump.jumpTo(exceptionOccuredChain));
        chainRegistry.createChain(PRE_PROCESS, jumpTo(processHeadersChain), jumpTo(exceptionOccuredChain));

        return chainRegistry;
    }
}
