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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class EscapedText {
    private final List<String> lines;

    static EscapedText escapedText(final String text) {
        validateNotNull(text, "text");
        final List<String> list = escapeNewlines(text, line -> line);
        return new EscapedText(list);
    }

    int maxLineLength() {
        return lines.stream()
                .mapToInt(String::length)
                .max().getAsInt();
    }

    List<String> renderLines(final int width) {
        final List<String> renderedLines = new LinkedList<>();
        lines.forEach(line -> {
            final List<String> chunks = splitInChunks(line, width);
            renderedLines.addAll(chunks);
        });
        return renderedLines;
    }

    private List<String> splitInChunks(final String string, final int width) {
        final List<String> chunks = new LinkedList<>();
        final int length = string.length();
        for (int i = 0; i < length; i += width) {
            final String chunk = string.substring(i, Math.min(length, i + width));
            chunks.add(chunk);
        }
        return chunks;
    }

    private static List<String> escapeNewlines(final String text, final Function<String, String> operation) {
        final String[] lines = text.split("\n");
        final List<String> transformed = new LinkedList<>();
        for (int i = 0; i < lines.length; ++i) {
            final String line = lines[i];
            final String transformedLine;
            if (i < lines.length - 1) {
                transformedLine = operation.apply(line);
            } else {
                transformedLine = line;
            }
            transformed.add(transformedLine);
        }
        return transformed;
    }
}
