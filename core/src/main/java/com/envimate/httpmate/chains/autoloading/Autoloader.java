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

package com.envimate.httpmate.chains.autoloading;

import com.envimate.httpmate.chains.ChainModule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.lang.Thread.currentThread;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public final class Autoloader {

    private Autoloader() {
    }

    public static Optional<ChainModule> loadModule(final String fullyQualifiedClassName) {
        validateNotNullNorEmpty(fullyQualifiedClassName, "fullyQualifiedClassName");
        return loadClass(fullyQualifiedClassName).map(clazz -> {
            final Method staticInitializer = findStaticInitializer(clazz);
            return invoke(staticInitializer);
        });
    }

    @SuppressWarnings("unchecked")
    private static Optional<Class<? extends ChainModule>> loadClass(final String fullyQualifiedClassName) {
        final ClassLoader classLoader = currentThread().getContextClassLoader();
        try {
            final Class<? extends ChainModule> clazz =
                    (Class<? extends ChainModule>) classLoader.loadClass(fullyQualifiedClassName);
            return of(clazz);
        } catch (final ClassNotFoundException e) {
            return empty();
        }
    }

    private static Method findStaticInitializer(final Class<? extends ChainModule> clazz) {
        final Method[] methods = clazz.getMethods();
        return stream(methods)
                .filter(method -> isPublic(method.getModifiers()))
                .filter(method -> isStatic(method.getModifiers()))
                .filter(method -> method.getReturnType().equals(clazz))
                .filter(method -> method.getParameterCount() == 0)
                .findFirst()
                .orElseThrow();
    }

    private static ChainModule invoke(final Method staticInitializer) {
        try {
            return (ChainModule) staticInitializer.invoke(null);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
