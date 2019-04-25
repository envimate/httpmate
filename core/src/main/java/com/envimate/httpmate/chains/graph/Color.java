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

import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Color {
    public static final Color BLACK = color("black");
    public static final Color RED = color("red4");
    public static final Color BLUE = color("navy");
    public static final Color YELLOW = color("yellow");
    public static final Color ORANGE = color("orangered2");
    public static final Color GREEN = color("green4");
    public static final Color VIOLET = color("violetred4");
    public static final Color PURPLE = color("purple4");

    private final String color;

    private static Color color(final String color) {
        validateNotNullNorEmpty(color, "color");
        return new Color(color);
    }

    public String color() {
        return color;
    }

    public String colorized(final String string) {
        return format("<font color=\"%s\">%s</font>", color, string);
    }
}
