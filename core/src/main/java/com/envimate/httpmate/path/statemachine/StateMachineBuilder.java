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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.envimate.httpmate.path.statemachine.State.aState;
import static com.envimate.httpmate.path.statemachine.TransitionFunction.transitionFunction;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class StateMachineBuilder<T> {
    private int stateNumber;
    private State initialState;
    private final List<State> finalState;
    private final Map<State, List<Transition<T>>> transitions;

    public static <T> StateMachineBuilder<T> stateMachineBuilder() {
        return new StateMachineBuilder<>(0, null, new LinkedList<>(), new HashMap<>());
    }

    public State createState() {
        final State state = aState(stateNumber);
        stateNumber = stateNumber + 1;
        if(initialState == null) {
            initialState = state;
        }
        return state;
    }

    public void addTransition(final State state, final Transition<T> transition) {
        final List<Transition<T>> transitionsForState = this.transitions.computeIfAbsent(state, s -> new LinkedList<>());
        transitionsForState.add(transition);
    }

    public void markAsFinal(final State state) {
        finalState.add(state);
    }

    public StateMachine<T> build() {
        final TransitionFunction<T> transitionFunction = transitionFunction(transitions);
        return StateMachine.stateMachine(initialState, transitionFunction, finalState);
    }
}
