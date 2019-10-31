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

package com.envimate.httpmate.documentation.authentication;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;

public final class InMemoryUserDatabase implements UserDatabase {
    private static final byte[] salt = createSalt();

    private final Map<String, String> passwordsByUsers;
    private final List<String> admins;

    public InMemoryUserDatabase() {
        passwordsByUsers = Map.of(
                "joe", hashPassword("qrpk4L?>L(DB\\[mN"),
                "jim", hashPassword(":Ce<9q=8KKj\\tgfK"),
                "jack", hashPassword("*eG)r@;{'4g'cM?3")
        );
        admins = Collections.singletonList("jack");
    }

    @Override
    public boolean authenticate(final String username, final String password) {
        if (isNull(username)) {
            return false;
        }
        if (isNull(password)) {
            return false;
        }
        if (!passwordsByUsers.containsKey(username)) {
            return false;
        }
        final String actualPasswordHash = hashPassword(password);
        final String expectedPasswordHash = passwordsByUsers.get(username);
        return actualPasswordHash.equals(expectedPasswordHash);
    }

    @Override
    public boolean hasAdminRights(final String username) {
        if (isNull(username)) {
            return false;
        }
        return admins.contains(username);
    }

    private static String hashPassword(final String password) {
        final KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        try {
            final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            final byte[] hash = factory.generateSecret(spec).getEncoded();
            return new String(hash, UTF_8);
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Unexpected error during password hashing", e);
        }
    }

    private static byte[] createSalt() {
        final SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }
}
