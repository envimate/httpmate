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

package com.envimate.httpmate.path;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.envimate.httpmate.util.Validators.validateNotNull;

final class PathResolver {

    private PathResolver() {
    }

    static String resolvePath(final String base, final String userProvidedAppendix) {
        validateNotNull(base, "base");
        validateNotNull(userProvidedAppendix, "userProvidedAppendix");
        final String absoluteBase = makeAbsolute(base);
        final Path basePath = Paths.get(absoluteBase);
        final String relativeUserProvidedAppendix = makeRelative(userProvidedAppendix);
        final Path userProvidedAppendixPath = Paths.get(relativeUserProvidedAppendix);
        final Path resolvedPath = basePath.resolve(userProvidedAppendixPath).normalize();
        if (!resolvedPath.startsWith(basePath)) {
            throw new IllegalArgumentException("User path escapes the base path");
        }
        return resolvedPath.toString();
    }

    private static String makeRelative(final String path) {
        if(!path.startsWith("/")) {
            return path;
        }
        final String withoutFirst = path.substring(1);
        return makeRelative(withoutFirst);
    }

    private static String makeAbsolute(final String path) {
        if(path.startsWith("/")) {
            return path;
        }
        return "/" + path;
    }
}
