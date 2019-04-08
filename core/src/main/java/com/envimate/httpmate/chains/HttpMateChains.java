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

import static com.envimate.httpmate.chains.ChainName.chainName;

public final class HttpMateChains {

    private HttpMateChains() {
    }

    public static final ChainName PRE_PROCESS = chainName("PRE_PROCESS");
    public static final ChainName PROCESS_HEADERS = chainName("PROCESS_HEADERS");
    public static final ChainName PROCESS_BODY = chainName("PROCESS_BODY");
    public static final ChainName PROCESS_SINGLE_PART = chainName("PROCESS_SINGLE_PART");
    public static final ChainName PROCESS_BODY_STRING = chainName("PROCESS_BODY_STRING");
    public static final ChainName PRE_DETERMINE_EVENT = chainName("PRE_DETERMINE_EVENT");
    public static final ChainName DETERMINE_EVENT = chainName("DETERMINE_EVENT");
    public static final ChainName AUTHENTICATION = chainName("AUTHENTICATION");
    public static final ChainName AUTHORIZATION = chainName("AUTHORIZATION");
    public static final ChainName PRE_MAP_TO_EVENT = chainName("PRE_MAP_TO_EVENT");
    public static final ChainName MAP_TO_EVENT = chainName("MAP_TO_EVENT");
    public static final ChainName SUBMIT = chainName("SUBMIT");
    public static final ChainName PRE_SERIALIZATION = chainName("PRE_SERIALIZATION");
    public static final ChainName SERIALIZATION = chainName("SERIALIZATION");
    public static final ChainName POST_SERIALIZATION = chainName("POST_SERIALIZATION");
    public static final ChainName EXCEPTION_OCCURRED = chainName("EXCEPTION_OCCURRED");
}
