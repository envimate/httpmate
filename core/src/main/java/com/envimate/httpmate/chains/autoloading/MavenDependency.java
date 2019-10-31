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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MavenDependency {
    private final String groupId;
    private final String artifactId;
    private final String version;

    public static MavenDependency mavenDependency(final String groupId,
                                                  final String artifactId,
                                                  final String version) {
        validateNotNullNorEmpty(groupId, "groupId");
        validateNotNullNorEmpty(artifactId, "artifactId");
        validateNotNullNorEmpty(version, "version");
        return new MavenDependency(groupId, artifactId, version);
    }

    public String render() {
        return format("" +
                        "<dependency>%n" +
                        "   <groupId>%s</groupId>%n" +
                        "   <artifactId>%s</artifactId>%n" +
                        "   <version>%s</version>%n" +
                        "</dependency>%n",
                groupId, artifactId, version);
    }
}
