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

import com.envimate.httpmate.path.statemachine.StateMachineMatcher;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.regex.Pattern.compile;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegexMatcher implements StateMachineMatcher<String> {
    private static final Pattern PATTERN = compile("\\|(.*)\\|");
    private static final Pattern NAMES_PATTERN = compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");
    private final Pattern regex;
    private final List<String> names;

    static boolean isRegex(final String stringSpecification) {
        final Matcher matcher = PATTERN.matcher(stringSpecification);
        return matcher.matches();
    }

    static StateMachineMatcher<String> fromStringSpecification(final String stringSpecification) {
        validateNotNullNorEmpty(stringSpecification, "stringSpecification");
        final Matcher matcher = PATTERN.matcher(stringSpecification);
        if (!matcher.matches()) {
            throw new RuntimeException("Not a regex: " + stringSpecification);
        }
        final String regex = matcher.group(1);

        final Matcher namesMatcher = NAMES_PATTERN.matcher(regex);
        final List<String> names = new LinkedList<>();
        while (namesMatcher.find()) {
            final String group = namesMatcher.group(1);
            names.add(group);
        }
        final Pattern regexPattern = compile(regex);

        return new RegexMatcher(regexPattern, names);
    }

    @Override
    public Optional<Map<String, String>> matchAndReturnCaptures(final String element) {
        final Matcher matcher = regex.matcher(element);
        if (!matcher.matches()) {
            return empty();
        }

        final Map<String, String> captures = new HashMap<>();
        names.forEach(name -> {
            try {
                final String value = matcher.group(name);
                captures.put(name, value);
            } catch (final IllegalArgumentException ignored) {
                // do nothing
            }
        });
        return of(captures);
    }
}
