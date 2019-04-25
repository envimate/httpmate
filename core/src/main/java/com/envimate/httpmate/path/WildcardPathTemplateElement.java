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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class WildcardPathTemplateElement implements PathTemplateElement {

    private static final Pattern PATTERN = Pattern.compile("<(.*)>");
    private final String name;

    static boolean isWildcard(final String stringSpecification) {
        final Matcher matcher = PATTERN.matcher(stringSpecification);
        return matcher.matches();
    }

    static PathTemplateElement fromStringSpecification(final String stringSpecification) {
        validateNotNullNorEmpty(stringSpecification, "stringSpecification");
        final Matcher matcher = PATTERN.matcher(stringSpecification);
        if(!matcher.matches()) {
            throw new RuntimeException("Not a wildcard: " + stringSpecification);
        }
        final String name = matcher.group(1);
        return new WildcardPathTemplateElement(name);
    }

    @Override
    public boolean matches(final String pathElement) {
        return true;
    }

    public String getName() {
        return this.name;
    }
}
