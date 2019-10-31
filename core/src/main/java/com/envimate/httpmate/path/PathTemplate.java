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

import com.envimate.httpmate.path.statemachine.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static com.envimate.httpmate.path.AnyMatcher.anyMatcher;
import static com.envimate.httpmate.path.AnyMatcher.isRecursiveWildcard;
import static com.envimate.httpmate.path.CaptureMatcher.isWildcard;
import static com.envimate.httpmate.path.statemachine.ElementPosition.start;
import static com.envimate.httpmate.path.statemachine.StateMachineBuilder.stateMachineBuilder;
import static com.envimate.httpmate.path.statemachine.Transition.transition;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathTemplate {
    private final List<String> elements;
    private final StateMachine<String> stateMachine;

    public static PathTemplate pathTemplate(final String asString) {
        final List<String> elementsAsStrings = splitIntoElements(asString);
        final List<StateMachineMatcher<String>> matchers = elementsAsStrings.stream()
                .map(PathTemplate::elementFromStringSpecification)
                .collect(toList());

        final StateMachineBuilder<String> stateMachineBuilder = stateMachineBuilder();
        State currentState = stateMachineBuilder.createState();
        for(final StateMachineMatcher<String> matcher : matchers) {
            if(matcher instanceof AnyMatcher) {
                stateMachineBuilder.addTransition(currentState, transition(matcher, currentState));
            } else {
                final State nextState = stateMachineBuilder.createState();
                stateMachineBuilder.addTransition(currentState, transition(matcher, nextState));
                currentState = nextState;
            }
        }
        stateMachineBuilder.markAsFinal(currentState);
        final StateMachine<String> stateMachine = stateMachineBuilder.build();

        return new PathTemplate(elementsAsStrings, stateMachine);
    }

    public boolean matches(final Path path) {
        return match(path).isSuccessful();
    }

    public Map<String, String> extractPathParameters(final Path path) {
        return match(path).captures();
    }

    private MatchingResult match(final Path path) {
        final String raw = path.raw();
        final List<String> elementsAsStrings = splitIntoElements(raw);
        final ElementPosition<String> inputPosition = start(elementsAsStrings);
        return stateMachine.accept(inputPosition);
    }

    public String toString() {
        return this.elements.stream()
                .collect(joining("/", "/", ""));
    }

    private static StateMachineMatcher<String> elementFromStringSpecification(final String stringSpecification) {
        if (isRecursiveWildcard(stringSpecification)) {
            return anyMatcher();
        }
        if (isWildcard(stringSpecification)) {
            return CaptureMatcher.fromStringSpecification(stringSpecification);
        }
        if(RegexMatcher.isRegex(stringSpecification)) {
            return RegexMatcher.fromStringSpecification(stringSpecification);
        }
        return StaticMatcher.fromStringSpecification(stringSpecification);
    }

    private static List<String> splitIntoElements(final String pathAsString) {
        return stream(pathAsString.split("/"))
                .filter(string -> !string.isEmpty())
                .collect(toList());
    }
}
