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

package com.envimate.httpmate.events;

import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.chains.Processor;

import java.util.Map;
import java.util.Optional;

import static com.envimate.httpmate.events.EventModule.EVENT;
import static java.util.Optional.empty;

public interface ExternalEventMapping extends Processor {

    void handle(Map<String, Object> event);

    @Override
    default void apply(final MetaData metaData) {
        final Map<String, Object> event = metaData.get(EVENT);
        handle(event);
    }

    default Optional<ChainName> jumpTarget() {
        return empty();
    }
}
