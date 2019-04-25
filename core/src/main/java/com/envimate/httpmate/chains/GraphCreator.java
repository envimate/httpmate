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

package com.envimate.httpmate.chains;

import com.envimate.httpmate.chains.graph.*;
import com.envimate.httpmate.chains.rules.*;

import java.util.*;

import static com.envimate.httpmate.chains.graph.Color.GREEN;
import static com.envimate.httpmate.chains.graph.Color.RED;
import static com.envimate.httpmate.chains.graph.ColorPool.colorPool;
import static com.envimate.httpmate.chains.graph.Edge.edge;
import static com.envimate.httpmate.chains.graph.Graph.graph;
import static com.envimate.httpmate.chains.graph.Label.emptyLabel;
import static com.envimate.httpmate.chains.graph.Label.htmlLabel;
import static com.envimate.httpmate.chains.graph.Node.node;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

final class GraphCreator {
    private static final Node CONSUME_NODE = node("CONSUME", GREEN);

    private GraphCreator() {
    }

    static Graph createGraph(final Map<ChainName, Chain> chains,
                             final boolean withExceptionRoutes) {
        validateNotNull(chains, "chains");
        final ColorPool<ModuleIdentifier> colorPool = colorPool();

        final Map<ChainName, Node> nodes = new HashMap<>();
        chains.forEach((name, chain) -> nodes.put(name, createNode(chain, colorPool)));

        final List<Edge> edges = new LinkedList<>();
        chains.forEach((name, chain) -> {
            final Action defaultAction = chain.defaultAction();
            final Color color = colorPool.assign(chain.getModuleIdentifier());
            createEdge(name, defaultAction, color, emptyLabel(), nodes).ifPresent(edges::add);

            chain.rules().stream()
                    .map(rule -> createRuleEdge(name, rule, colorPool, nodes))
                    .flatMap(Optional::stream)
                    .forEach(edges::add);

            if (withExceptionRoutes) {
                final Action exceptionAction = chain.exceptionAction();
                createEdge(name, exceptionAction, RED, emptyLabel(), nodes).ifPresent(edges::add);
            }
        });
        return graph(nodes.values(), edges);
    }

    private static Node createNode(final Chain chain,
                                   final ColorPool<ModuleIdentifier> colorPool) {
        final ModuleIdentifier moduleIdentifier = chain.getModuleIdentifier();
        final Color color = colorPool.assign(moduleIdentifier);
        final String name = chain.getName().name();
        final Label label = createLabel(chain, colorPool);
        return node(name, label, color);
    }

    private static Label createLabel(final Chain chain,
                                     final ColorPool<ModuleIdentifier> colorPool) {
        final StringBuilder labelBuilder = new StringBuilder();
        final Color color = colorPool.assign(chain.getModuleIdentifier());
        labelBuilder.append(color.colorized(chain.getName().name()));
        chain.processors().forEach(processor -> {
            final Color processorColor = colorPool.assign(processor.identifier());
            labelBuilder.append("<br/>");
            labelBuilder.append(processorColor.colorized(processor.processor().identifier()));
        });
        return htmlLabel(labelBuilder.toString());
    }

    private static Optional<Edge> createRuleEdge(final ChainName from,
                                                 final Rule rule,
                                                 final ColorPool<ModuleIdentifier> colorPool,
                                                 final Map<ChainName, Node> nodes) {
        final Color color = colorPool.assign(rule.moduleIdentifier());
        final Action action = rule.action();
        final RuleDescription description = rule.description();

        final String label;
        if (action instanceof Jump) {
            label = color.colorized(description.value().orElse("?"));
        } else if (action instanceof Consume || action instanceof Drop) {
            label = color.colorized(description.value().orElse(""));
        } else {
            throw new UnsupportedOperationException();
        }

        return createEdge(from, action, color, htmlLabel(label), nodes);
    }

    private static Optional<Edge> createEdge(final ChainName from,
                                             final Action action,
                                             final Color color,
                                             final Label label,
                                             final Map<ChainName, Node> nodes) {
        final Node fromNode = nodes.get(from);
        if (action instanceof Jump) {
            final Jump jump = (Jump) action;
            final ChainName to = jump.target().orElseThrow();
            return of(edge(fromNode, nodes.get(to), color, label));
        }
        if (action instanceof Consume) {
            return of(edge(fromNode, CONSUME_NODE, color, label));
        }
        if (action instanceof Drop) {
            return empty();
        }
        throw new UnsupportedOperationException();
    }
}
