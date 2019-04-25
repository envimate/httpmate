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

package com.envimate.httpmate.multipart;

import com.envimate.httpmate.chains.ChainExtender;
import com.envimate.httpmate.chains.ChainModule;
import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.chains.Configurator;
import com.envimate.httpmate.http.ContentType;
import com.envimate.httpmate.http.Http;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.HttpMateChainKeys.CONTENT_TYPE;
import static com.envimate.httpmate.HttpMateChains.*;
import static com.envimate.httpmate.chains.ChainName.chainName;
import static com.envimate.httpmate.chains.Configurator.toUseModules;
import static com.envimate.httpmate.chains.rules.Jump.jumpTo;
import static com.envimate.httpmate.http.ContentType.fromString;
import static com.envimate.httpmate.multipart.MultipartProcessor.multipartProcessor;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultipartModule implements ChainModule {
    private static final ChainName PROCESS_BODY_MULTIPART = chainName("PROCESS_BODY_MULTIPART");
    private static final ContentType CONTENT_TYPE_PREFIX = fromString("multipart/form-data");
    private static final String RULE_DESCRIPTION = format("%s=%s", Http.Headers.CONTENT_TYPE,
            CONTENT_TYPE_PREFIX.internalValueForMapping());

    public static Configurator toExposeMultipartBodiesUsingMultipartIteratorBody() {
        return toUseModules(new MultipartModule());
    }

    @Override
    public void register(final ChainExtender extender) {
        extender.createChain(PROCESS_BODY_MULTIPART, jumpTo(DETERMINE_HANDLER), jumpTo(EXCEPTION_OCCURRED));
        extender.addProcessor(PROCESS_BODY_MULTIPART, multipartProcessor());

        extender.routeIf(PROCESS_BODY, jumpTo(PROCESS_BODY_MULTIPART), CONTENT_TYPE,
                contentType -> contentType.startsWith(CONTENT_TYPE_PREFIX), RULE_DESCRIPTION);
    }
}
