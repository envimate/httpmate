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

package com.envimate.httpmate.path;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathTemplate {

    private final List<PathTemplateElement> templateElements;

    public static PathTemplate pathTemplate(final String asString) {
        final String[] elementsAsStrings = splitIntoElements(asString);
        final List<PathTemplateElement> elements = stream(elementsAsStrings)
                .map(PathTemplateElement::fromStringSpecification)
                .collect(toList());
        return new PathTemplate(elements);
    }

    public boolean matches(final String path) {
        final String[] elementsAsStrings = splitIntoElements(path);
        final int numberOfElements = elementsAsStrings.length;
        if(this.templateElements.size() != numberOfElements) {
            return false;
        }
        for(int i = 0; i < numberOfElements; ++i) {
            final PathTemplateElement pathTemplateElement = this.templateElements.get(i);
            final String pathElement = elementsAsStrings[i];
            if(!pathTemplateElement.matches(pathElement)) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return this.templateElements
                .stream()
                .map(PathTemplateElement::toString)
                .collect(Collectors.joining("/", "/", ""));
    }

    public Map<String, String> extractPathParameters(final String path) {
        if(!matches(path)) {
            throw new RuntimeException("Can only extract path parameters from matching paths.");
        }
        final String[] elements = splitIntoElements(path);
        final Map<String, String> pathParameters = new HashMap<>();
        final int length = elements.length;
        for(int i = 0; i < length; i++) {
            final PathTemplateElement templateElement = this.templateElements.get(i);
            if(!(templateElement instanceof WildcardPathTemplateElement)) {
                continue;
            }
            final WildcardPathTemplateElement wildcardTemplateElement = (WildcardPathTemplateElement) templateElement;
            final String name = wildcardTemplateElement.getName();
            final String value = elements[i];
            pathParameters.put(name, value);
        }
        return pathParameters;
    }

    private static String[] splitIntoElements(final String pathAsString) {
        return stream(pathAsString.split("/"))
                .filter(string -> !string.isEmpty())
                .toArray(String[]::new);
    }
}
