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

package com.envimate.httpmate.chains;

import lombok.*;

import java.util.function.Consumer;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ProcessingContext {
    @Getter
    private final ChainRegistry chainRegistry;
    @Getter
    private final MetaData metaData;
    @Getter
    private final Consumer<MetaData> consumer;

    static ProcessingContext processingContext(final ChainRegistry chainRegistry,
                                               final MetaData metaData,
                                               final Consumer<MetaData> consumer) {
        validateNotNull(chainRegistry, "chainRegistry");
        validateNotNull(metaData, "metaData");
        validateNotNull(consumer, "consumer");
        return new ProcessingContext(chainRegistry, metaData, consumer);
    }
}
