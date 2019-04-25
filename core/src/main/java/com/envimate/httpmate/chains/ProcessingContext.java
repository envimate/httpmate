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

package com.envimate.httpmate.chains;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Consumer;

import static com.envimate.httpmate.chains.RunId.randomRunId;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ProcessingContext {
    private final RunId runId;
    private final MetaData metaData;
    private final Consumer<MetaData> consumer;

    static ProcessingContext processingContext(final MetaData metaData,
                                               final Consumer<MetaData> consumer) {
        validateNotNull(metaData, "metaData");
        validateNotNull(consumer, "consumer");
        final RunId runId = randomRunId();
        return new ProcessingContext(runId, metaData, consumer);
    }

    MetaData metaData() {
        return metaData;
    }

    void consume() {
        consumer.accept(metaData);
    }

    RunId runId() {
        return runId;
    }
}
