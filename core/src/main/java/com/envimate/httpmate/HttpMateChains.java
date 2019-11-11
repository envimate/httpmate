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

package com.envimate.httpmate;

import com.envimate.httpmate.chains.ChainName;

import static com.envimate.httpmate.chains.ChainName.chainName;

public final class HttpMateChains {

    private HttpMateChains() {
    }

    public static final ChainName INIT = chainName("INIT");
    public static final ChainName PRE_PROCESS = chainName("PRE_PROCESS");
    public static final ChainName PROCESS_HEADERS = chainName("PROCESS_HEADERS");
    public static final ChainName PROCESS_BODY = chainName("PROCESS_BODY");
    public static final ChainName PROCESS_BODY_STRING = chainName("PROCESS_BODY_STRING");
    public static final ChainName DETERMINE_HANDLER = chainName("DETERMINE_HANDLER");
    public static final ChainName PREPARE_RESPONSE = chainName("PREPARE_RESPONSE");
    public static final ChainName INVOKE_HANDLER = chainName("INVOKE_HANDLER");
    public static final ChainName POST_INVOKE = chainName("POST_INVOKE");

    public static final ChainName EXCEPTION_OCCURRED = chainName("EXCEPTION_OCCURRED");
    public static final ChainName PREPARE_EXCEPTION_RESPONSE = chainName("PREPARE_EXCEPTION_RESPONSE");
    public static final ChainName MAP_EXCEPTION_TO_RESPONSE = chainName("MAP_EXCEPTION_TO_RESPONSE");

    public static final ChainName ERROR = chainName("ERROR");

    public static final ChainName POST_PROCESS = chainName("POST_PROCESS");
}
