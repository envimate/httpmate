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

package websockets.exampleproject.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static websockets.exampleproject.domain.Password.password;
import static websockets.exampleproject.domain.User.user;
import static websockets.exampleproject.domain.Username.username;

public final class UserRepository {
    private static final Map<Username, User> USERS = new HashMap<>();

    static {
        addUser("a", "a");
        addUser("r", "r");
        addUser("m", "m");
    }

    private static void addUser(final String name,
                                final String password) {
        final Username username = username(name);
        USERS.put(username, user(username, password(password)));
    }

    public static UserRepository userRepository() {
        return new UserRepository();
    }

    public Optional<User> getIfCorrectAuthenticationInformation(final String username,
                                                                final String password) {
        final Username key = username(username);
        if(USERS.containsKey(key)) {
            final User user = USERS.get(key);
            if (user.password().matches(password(password))) {
                return of(user);
            }
        }
        return empty();
    }
}
