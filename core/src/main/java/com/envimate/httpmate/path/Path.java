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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.path.PathTemplate.pathTemplate;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Path {
    private final String path;

    public static Path path(final String path) {
        validateNotNull(path, "path");
        return new Path(path);
    }

    public boolean matches(final String template) {
        final PathTemplate pathTemplate = pathTemplate(template);
        return pathTemplate.matches(this);
    }

    public Path cutPrefix(final String prefix) {
        validateNotNull(prefix, "prefix");
        if (!path.startsWith(prefix)) {
            throw new IllegalArgumentException(format("Path '%s' does not start with '%s'", path, prefix));
        }
        final String cutPath = path.substring(prefix.length());
        return new Path(cutPath);
    }

    public Path safelyRebaseTo(final String prefix) {
        final String rebasedPath = PathResolver.resolvePath(prefix, path);
        return path(rebasedPath);
    }

    public String raw() {
        return path;
    }
}
