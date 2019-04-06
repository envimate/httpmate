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

package com.envimate.httpmate.processors;

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.chains.rules.Processor;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.processingContext.ProcessingContext;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.envimate.httpmate.chains.HttpMateChainKeys.EVENT;
import static com.envimate.httpmate.chains.HttpMateChainKeys.EVENT_RETURN_VALUE;
import static com.envimate.httpmate.chains.HttpMateChainKeys.EVENT_TYPE;
import static com.envimate.httpmate.processors.EventDispatchingException.eventDispatchingException;
import static com.envimate.messageMate.messageFunction.MessageFunctionBuilder.aMessageFunction;
import static java.lang.Thread.currentThread;
import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DispatchEventProcessor implements Processor {
    private final MessageFunction messageFunction;

    public static Processor dispatchEventProcessor(final MessageBus messageBus) {
        final MessageFunction messageFunction = aMessageFunction(messageBus);
        return new DispatchEventProcessor(messageFunction);
    }

    @Override
    public void apply(final MetaData metaData) {
        final EventType eventType = metaData.get(EVENT_TYPE);
        final Object event = metaData.get(EVENT);
        final ResponseFuture request = messageFunction.request(eventType, event);
        try {
            final ProcessingContext<Map<String, Object>> raw = (ProcessingContext<Map<String, Object>>) (Object) request.getRaw();
            if (raw.getErrorPayload() != null) {
                final Map<String, Object> errorPayload = (Map<String, Object>) raw.getErrorPayload();
                throw eventDispatchingException((Throwable) errorPayload.get("Exception"));
            }
            final Map<String, Object> response = raw.getPayload();
            metaData.set(EVENT_RETURN_VALUE, ofNullable(response));
        } catch (final InterruptedException e) {
            request.cancel(true);
            currentThread().interrupt();
        } catch (final ExecutionException e) {
            throw eventDispatchingException(e.getCause());
        }
    }
}
