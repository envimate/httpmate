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

package com.envimate.httpmate.util.describing;

import static java.lang.String.format;

final class DescriptionUtils {
    static final String HORIZONTAL_THICK = "=";
    static final String HORIZONTAL_THIN = "-";
    static final String VERTICAL = "|";

    private DescriptionUtils() {
    }

    static String center(final String string, final int width) {
        final int length = string.length();
        final int missingWhitespace = width - length;
        final int leftWhitespaceLength = missingWhitespace / 2;
        final String leftWhitespace = times(" ", leftWhitespaceLength);
        return pad(format("%s%s", leftWhitespace, string), width);
    }

    static String pad(final String string, final int width) {
        final int length = string.length();
        final int whitespaceLength = width - length;
        final String whitespace = times(" ", whitespaceLength);
        return format("%s%s", string, whitespace);
    }

    static String times(final String sequence, final int times) {
        if(times < 0) {
            return "";
        }
        return sequence.repeat(times);
    }
}
