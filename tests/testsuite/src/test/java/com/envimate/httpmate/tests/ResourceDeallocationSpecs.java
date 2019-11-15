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

package com.envimate.httpmate.tests;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;

public final class ResourceDeallocationSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void createTestAndDestroyALotOfTimes(final TestEnvironment testEnvironment) {
        final Boolean runThisTest = Optional.ofNullable(System.getProperty("testMode")).map(s -> s.equals("RELEASE")).orElse(false);
        if (runThisTest) {
            final Instant start = Instant.now();
            int max = 100000;
            Instant partStart = Instant.now();
            for (int i = 0; i < max; i++) {
                if (i % 100 == 0) {
                    final Duration durationPerTest = Duration.between(partStart, Instant.now()).dividedBy(100);
                    System.out.println(i + "/" + max + ", " + durationPerTest.toNanos() / 1000000d + "ms/testCall");
                    partStart = Instant.now();
                }
                try (final HttpMate httpMate = anHttpMate()
                        .get("/hello", (request, response) -> response.setBody("World"))
                        .build()) {
                    testEnvironment.given(httpMate)
                            .when()
                            .aRequestToThePath("/hello").viaTheGetMethod().withAnEmptyBody().isIssued()
                            .theResponseBodyWas("World");
                }
            }
            System.out.println("Took " + Duration.between(start, Instant.now()).dividedBy(max).toMillis() + "ms/testCall");
        } else {
            System.out.println("Skipping this test, since system property testMode is not set to RELEASE");
        }
    }
}
