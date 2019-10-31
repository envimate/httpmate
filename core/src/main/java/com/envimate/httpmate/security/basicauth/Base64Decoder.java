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

package com.envimate.httpmate.security.basicauth;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getDecoder;

final class Base64Decoder {
    private static final Base64.Decoder DECODER = getDecoder();

    private Base64Decoder() {
    }

    static String decodeBase64(final String encoded) {
        final byte[] decoded = DECODER.decode(encoded);
        return new String(decoded, UTF_8);
    }
}
