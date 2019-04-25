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

package com.envimate.httpmate.chains.graph;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Graph {
    private final Collection<Node> nodes;
    private final Collection<Edge> edges;

    public static Graph graph(final Collection<Node> nodes,
                              final Collection<Edge> edges) {
        validateNotNull(nodes, "nodes");
        validateNotNull(edges, "edges");
        return new Graph(nodes, edges);
    }

    public String plot() {
        final StringBuilder builder = new StringBuilder();
        builder.append("digraph G {\n");
        builder.append("splines=ortho;\n");
        builder.append("CONSUME [shape=point, width=0.5];\n");
        nodes.stream().map(Node::plot).forEach(builder::append);
        edges.stream().map(Edge::plot).forEach(builder::append);
        builder.append("}\n");
        return builder.toString();
    }
}
