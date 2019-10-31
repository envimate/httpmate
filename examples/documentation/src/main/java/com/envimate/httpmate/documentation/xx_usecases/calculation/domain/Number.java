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

package com.envimate.httpmate.documentation.xx_usecases.calculation.domain;

import static java.lang.Integer.parseInt;

public final class Number {
    private final int number;

    public Number(final int number) {
        this.number = number;
    }

    public static Number parseNumber(final String numberAsString) {
        final int number = parseInt(numberAsString);
        return new Number(number);
    }

    public int value() {
        return number;
    }

    public String stringValue() {
        return String.valueOf(number);
    }
}
