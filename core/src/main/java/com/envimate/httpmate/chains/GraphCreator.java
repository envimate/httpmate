/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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

package com.envimate.httpmate.chains;

import com.envimate.httpmate.chains.rules.Action;
import com.envimate.httpmate.chains.rules.Consume;
import com.envimate.httpmate.chains.rules.Drop;
import com.envimate.httpmate.chains.rules.Jump;

import java.util.Map;

import static java.lang.String.format;

final class GraphCreator {

    private GraphCreator() {
    }

    static String createGraph(final Map<ChainName, Chain> chains) {
        final StringBuilder builder = new StringBuilder();
        builder.append("digraph G {\n");
        builder.append("CONSUMER [shape=point, width=0.5];\n");
        builder.append("DROP [shape=point, color=red, width=0.5];\n");
        chains.forEach((name, chain) -> builder.append(format("%s [label=\"%s\"];", name, name)).append("\n"));

        chains.forEach((name, chain) -> {
            final Action defaultAction = chain.getDefaultAction();
            builder.append(relation(name, defaultAction, "black", chains));

            chain.getRules().forEach(rule -> {
                final Action action = rule.action();
                builder.append(relation(name, action, "black", chains));
            });

            final Action exceptionAction = chain.getExceptionAction();
            builder.append(relation(name, exceptionAction, "red", chains));
        });

        builder.append("}\n");
        return builder.toString();
    }

    private static String relation(final ChainName from, final Action action, final String color, final Map<ChainName, Chain> chains) {
        if (action instanceof Jump) {
            final Jump jump = (Jump) action;
            final Chain chain = jump.target().orElseThrow();
            final ChainName targetName = nameForChain(chain);
            return fromTo(from, targetName.name(), color);
        }
        if (action instanceof Consume) {
            return fromTo(from, "CONSUMER", color);
        }
        if (action instanceof Drop) {
            return fromTo(from, "DROP", color);
        }
        return "";
    }

    private static String fromTo(final ChainName from, final String to, final String color) {
        return format("%s -> %s [color=\"%s\" ] ", from.name(), to, color) + "\n";
    }

    private static ChainName nameForChain(final Chain chain) {
        return chain.getName();
        /*
        return chains.entrySet().stream()
                .filter(entry -> entry.getValue().equals(chain))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(RuntimeException::new);
         */
    }
}
