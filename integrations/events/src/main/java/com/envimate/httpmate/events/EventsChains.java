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

import static com.envimate.httpmate.chains.ChainName.chainName;

public final class EventsChains {
    public static final ChainName MAP_REQUEST_TO_EVENT = chainName("MAP_REQUEST_TO_EVENT");
    public static final ChainName SUBMIT_EVENT = chainName("SUBMIT_EVENT");
    public static final ChainName MAP_EVENT_TO_RESPONSE = chainName("MAP_EVENT_TO_RESPONSE");
    public static final ChainName EXTERNAL_EVENT = chainName("EXTERNAL_EVENT");

    private EventsChains() {
    }
}
