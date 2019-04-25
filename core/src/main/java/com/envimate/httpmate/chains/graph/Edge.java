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
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Edge {
    private final Node from;
    private final Node to;
    private final Color color;
    private final Label label;

    public static Edge edge(final Node from,
                            final Node to,
                            final Color color,
                            final Label label) {
        validateNotNull(from, "from");
        validateNotNull(to, "to");
        validateNotNull(color, "color");
        validateNotNull(label, "label");
        return new Edge(from, to, color, label);
    }

    String plot() {
        return format("%s -> %s [color=\"%s\"; label=%s ];", from.name(), to.name(), color.color(), label.plot()) + "\n";
    }
}
