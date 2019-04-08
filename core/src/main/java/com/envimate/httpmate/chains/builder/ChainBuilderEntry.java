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

package com.envimate.httpmate.chains.builder;

import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.chains.rules.Processor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class ChainBuilderEntry {
    private final ChainName chainName;
    private final List<? extends Processor> processors;

    public static ChainBuilderEntry chainBuilderEntry(final ChainName chainName,
                                                      final List<? extends Processor> processors) {
        validateNotNull(chainName, "chainName");
        validateNotNull(processors, "processors");
        return new ChainBuilderEntry(chainName, processors);
    }

    public ChainName chainName() {
        return chainName;
    }

    public List<? extends Processor> processors() {
        return processors;
    }
}
