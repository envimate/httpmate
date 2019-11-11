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

package com.envimate.httpmate.tests.givenwhenthen;

import com.envimate.httpmate.tests.givenwhenthen.client.HttpClientResponse;
import com.envimate.httpmate.tests.lowlevel.LowLevelHttpMateConfiguration;

import java.util.HashMap;
import java.util.Map;

import static com.envimate.httpmate.tests.givenwhenthen.JsonNormalizer.normalizeJsonToMap;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public final class Then {

    private final HttpClientResponse response;

    private Then(final HttpClientResponse response) {
        this.response = response;
    }

    static Then then(final HttpClientResponse response) {
        return new Then(response);
    }

    public Then theStatusCodeWas(final int expectedStatusCode) {
        final int actualStatusCode = response.getStatusCode();
        assertThat(actualStatusCode, is(expectedStatusCode));
        return this;
    }

    public Then theResponseContentTypeWas(final String expectedContentType) {
        return theReponseContainsTheHeader("Content-Type", expectedContentType);
    }

    public Then theReponseContainsTheHeader(final String key, final String value) {
        final Map<String, String> headers = response.getHeaders();
        final Map<String, String> normalizedHeaders = new HashMap<>();
        headers.forEach((k, v) -> normalizedHeaders.put(k.toLowerCase(), v));
        final String normalizedKey = key.toLowerCase();
        assertThat(normalizedHeaders.keySet(), hasItem(normalizedKey));
        final String actualValue = normalizedHeaders.get(normalizedKey);
        assertThat(actualValue, is(value));
        return this;
    }

    public Then theResponseBodyWas(final String expectedResponseBody) {
        final String actualResponseBody = response.getBody();
        assertThat(actualResponseBody, is(expectedResponseBody));
        return this;
    }

    public Then theResponseBodyContains(final String expectedResponseBody) {
        final String actualResponseBody = response.getBody();
        assertThat(actualResponseBody, containsString(expectedResponseBody));
        return this;
    }

    public Then theLogOutputStartedWith(final String expectedPrefix) {
        final String logContent = LowLevelHttpMateConfiguration.logger.toString();
        assertThat(logContent, startsWith(expectedPrefix));
        return this;
    }

    public Then theJsonResponseEquals(final String expectedJson) {
        final Map<String, Object> normalizedExpected = normalizeJsonToMap(expectedJson);
        final String actualResponseBody = response.getBody();
        final Map<String, Object> normalizedActual = normalizeJsonToMap(actualResponseBody);
        assertThat(normalizedActual, is(normalizedExpected));
        return this;
    }
}
