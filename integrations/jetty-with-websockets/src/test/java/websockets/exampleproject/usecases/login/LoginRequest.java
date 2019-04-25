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

package websockets.exampleproject.usecases.login;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import websockets.exampleproject.domain.Password;
import websockets.exampleproject.domain.Username;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoginRequest {
    private final Username username;
    private final Password password;

    public static LoginRequest loginRequest(final Username username,
                                            final Password password) {
        validateNotNull(username, "username");
        validateNotNull(password, "password");
        return new LoginRequest(username, password);
    }
}
