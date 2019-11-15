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

package com.envimate.httpmate.tests.lowlevel.aaa;

import com.envimate.httpmate.tests.givenwhenthen.TestEnvironment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.http.Http.StatusCodes.BAD_REQUEST;
import static com.envimate.httpmate.security.SecurityConfigurators.toFilterRequestsThat;
import static com.envimate.httpmate.tests.givenwhenthen.TestEnvironment.ALL_ENVIRONMENTS;

public final class FilterSpecs {

    @ParameterizedTest
    @MethodSource(ALL_ENVIRONMENTS)
    public void requestsCanBeFiltered(final TestEnvironment testEnvironment) {
        testEnvironment.given(
                anHttpMate()
                        .get("/test", (request, response) -> response.setBody("test"))
                        .configured(toFilterRequestsThat(request -> request.headers().getOptionalHeader("illegal_header").isPresent())
                                .rejectingFilteredRequestsUsing((request, response) -> response.setStatus(BAD_REQUEST)))
                        .build()
        )
                .when().aRequestToThePath("/test").viaTheGetMethod().withAnEmptyBody().withTheHeader("illegal_header", "abc").isIssued()
                .theStatusCodeWas(400);
    }
}
