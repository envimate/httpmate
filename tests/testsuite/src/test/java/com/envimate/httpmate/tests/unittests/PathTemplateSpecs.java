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

package com.envimate.httpmate.tests.unittests;

import com.envimate.httpmate.path.Path;
import com.envimate.httpmate.path.PathTemplate;
import org.junit.Test;

import java.util.Map;

import static com.envimate.httpmate.path.Path.path;
import static com.envimate.httpmate.path.PathTemplate.pathTemplate;
import static java.util.Map.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class PathTemplateSpecs {

    @Test
    public void testPathTemplates() {
        matches("/", "/");
        matches("/test", "/test");
        matchesNot("/test/asdf", "/test");
        matchesNot("/test", "/test/asdf");
        matchesNot("/test", "/Test");
        matches("/<var>", "/test");
        matchesNot("/<var>/qwer", "/test");
        matches("/*", "/test");
        matches("/*", "/qwer/asdf");
        matches("/*", "/qwer/asdf/qwer");
        matches("/*/qwer", "/qwer/asdf/qwer");
        matchesNot("/*/qwer", "/asdf/asdf/asdf");
        matchesNot("/*/qwer", "/qwer/asdf/asdf");
        matches("/*/a/*/b/*/c", "/x/a/y/b/z/c");

        matches("/|.*|", "/a");
        matches("/|[abc]|", "/b");
        matchesNot("/|[abc]|", "/d");
        matches("/|\\d|", "/1");
        matches("/|\\d*|", "/1337");
        matchesNot("/|\\d|", "/a");
        matches("/|(?<qwer>\\d*)|", "/1337");
    }

    @Test
    public void testParameterExtraction() {
        parametersExtracted("/<var>", "/qwer", of("var", "qwer"));
        parametersExtracted("/<var1>/asdf/<var2>", "/qwer/asdf/yxcv", of("var1", "qwer", "var2", "yxcv"));
        parametersExtracted("/*/<var>", "/qwer", of("var", "qwer"));
        parametersExtracted("/*/<var>", "/asdf/qwer", of("var", "qwer"));
        parametersExtracted("/*/<var>/*", "/asdf/qwer", of("var", "asdf"));

        parametersExtracted("/|(?<qwer>\\d*)|", "/1337", of("qwer", "1337"));
        parametersExtracted("/|(?<qwer>\\d*)(?<asdf>\\w*)|", "/1337yxcv", of("qwer", "1337", "asdf", "yxcv"));
        parametersExtracted("/|(?<qwer>\\d*)(?<asdf>\\d*)|", "/1337", of("qwer", "1337", "asdf", ""));
    }

    private static void parametersExtracted(final String template,
                                            final String input,
                                            final Map<String, String> expectedParameters) {
        final PathTemplate pathTemplate = pathTemplate(template);
        final Path path = path(input);
        final Map<String, String> parameters = pathTemplate.extractPathParameters(path);
        expectedParameters.forEach((key, value) -> assertThat(parameters.get(key), is(value)));
    }

    private static void matches(final String template, final String input) {
        assertThat(pathMatches(template, input), is(true));
    }

    private static void matchesNot(final String template, final String input) {
        assertThat(pathMatches(template, input), is(false));
    }

    private static boolean pathMatches(final String template, final String input) {
        final PathTemplate pathTemplate = pathTemplate(template);
        final Path path = path(input);
        return pathTemplate.matches(path);
    }
}
