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

package com.envimate.httpmate.multipart;

import com.envimate.httpmate.Module;
import com.envimate.httpmate.chains.Chain;
import com.envimate.httpmate.chains.ChainRegistry;
import com.envimate.httpmate.chains.HttpMateChains;
import com.envimate.httpmate.request.ContentType;
import com.envimate.messageMate.messageBus.MessageBus;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.chains.ChainName.chainName;
import static com.envimate.httpmate.chains.HttpMateChainKeys.CONTENT_TYPE;
import static com.envimate.httpmate.chains.HttpMateChains.DETERMINE_EVENT;
import static com.envimate.httpmate.chains.HttpMateChains.EXCEPTION_OCCURRED;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.chains.rules.Rule.jumpRule;
import static com.envimate.httpmate.multipart.MultipartProcessor.multipartProcessor;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipartModule implements Module {
    private static final String CONTENT_TYPE_PREFIX = "multipart/form-data";

    public static Module multipartModule() {
        return new MultipartModule();
    }

    @Override
    public void register(final ChainRegistry chainRegistry, final MessageBus messageBus) {
        final Chain determineEventChain = chainRegistry.getChainFor(DETERMINE_EVENT);
        final Chain exceptionOccuredChain = chainRegistry.getChainFor(EXCEPTION_OCCURRED);

        final Chain processMultipartChain = chainRegistry
                .createChain(chainName("PROCESS_MULTIPART"), jumpTo(determineEventChain), jumpTo(exceptionOccuredChain));
        processMultipartChain.addProcessor(multipartProcessor());

        final Chain processBodyChain = chainRegistry.getChainFor(HttpMateChains.PROCESS_BODY);
        processBodyChain.addRoutingRule(jumpRule(processMultipartChain, metaData -> {
            final ContentType contentType = metaData.get(CONTENT_TYPE);
            return contentType.startsWith(ContentType.fromString(CONTENT_TYPE_PREFIX));
        }));
    }
}
