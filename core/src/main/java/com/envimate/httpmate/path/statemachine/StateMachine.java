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

package com.envimate.httpmate.path.statemachine;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.httpmate.path.statemachine.MatchingResult.fail;
import static com.envimate.httpmate.path.statemachine.MatchingResult.matchingResult;
import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StateMachine<T> {
    private final State initialState;
    private final TransitionFunction<T> transitionFunction;
    private final List<State> finalStates;

    static <T> StateMachine<T> stateMachine(final State initialState,
                                            final TransitionFunction<T> transitionFunction,
                                            final List<State> finalStates) {
        validateNotNull(initialState, "initialState");
        validateNotNull(transitionFunction, "transitionFunction");
        validateNotNull(finalStates, "finalStates");
        return new StateMachine<>(initialState, transitionFunction, finalStates);
    }

    public MatchingResult accept(final ElementPosition<T> elementPosition) {
        return accept(initialState, elementPosition);
    }

    private MatchingResult accept(final State state, final ElementPosition<T> elementPosition) {
        if (elementPosition.isEnd()) {
            return matchingResult(finalStates.contains(state));
        }
        final T element = elementPosition.get();
        final List<SuccessfulTransition> successfulTransitions = transitionFunction.transition(state, element);
        final ElementPosition<T> nextPosition = elementPosition.next();
        for (final SuccessfulTransition successfulTransition : successfulTransitions) {
            final State nextState = successfulTransition.nextState();
            final MatchingResult result = accept(nextState, nextPosition);
            if (result.isSuccessful()) {
                return result.merge(successfulTransition.captures());
            }
        }
        return fail();
    }
}
