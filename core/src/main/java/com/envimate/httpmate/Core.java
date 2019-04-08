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

import com.envimate.httpmate.chains.ChainRegistry;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.chains.rules.Processor;
import com.envimate.httpmate.convenience.preprocessors.Authenticator;
import com.envimate.httpmate.convenience.preprocessors.Authorizer;
import com.envimate.httpmate.event.EventTypeGenerators;
import com.envimate.httpmate.mapper.EventToResponseMapper;
import com.envimate.httpmate.mapper.ExceptionSerializer;
import com.envimate.httpmate.mapper.RequestToEventMapper;
import com.envimate.httpmate.mapper.ResponseTemplate;
import com.envimate.httpmate.mapper.filtermap.FilterMap;
import com.envimate.messageMate.messageBus.MessageBus;

import java.util.List;

import static com.envimate.httpmate.chains.HttpMateChains.*;
import static com.envimate.httpmate.chains.builder.ChainBuilder.startingAChainWith;
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
                                       final List<Authorizer> authorizers,
                                       final List<Processor> requestFilters) {
        return startingAChainWith(PRE_PROCESS, translateToValueObjectsProcessor())
                .append(PROCESS_HEADERS)
                .append(PROCESS_BODY)
                .append(PROCESS_SINGLE_PART)
                .append(PROCESS_BODY_STRING, streamToStringProcessor())
                .append(PRE_DETERMINE_EVENT)
                .append(DETERMINE_EVENT, determineEventProcessor(useCaseGenerators))
                .append(AUTHENTICATION, authenticators)
                .append(AUTHORIZATION, authorizers)
                .append(PRE_MAP_TO_EVENT, requestFilters)
                .append(MAP_TO_EVENT, mapToEventProcessor(requestToEventMappers))
                .append(SUBMIT, dispatchEventProcessor(messageBus))
                .append(PRE_SERIALIZATION, createHttpResponseBuilderProcessor(responseTemplate))
                .append(SERIALIZATION, serializationProcessor(eventToResponseMapper))
                .append(POST_SERIALIZATION, stringBodyToStreamProcessor())
                .withTheExceptionChain(EXCEPTION_OCCURRED,
                        createHttpResponseBuilderProcessor(responseTemplate),
                        mapExceptionProcessor(exceptionSerializer),
                        stringBodyToStreamProcessor());
    }
}
