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

package com.envimate.httpmate.documentation.xx_cookies;

import com.envimate.httpmate.HttpMate;

import static com.envimate.httpmate.HttpMate.anHttpMate;
import static com.envimate.httpmate.http.headers.cookies.CookieBuilder.cookie;
import static com.envimate.httpmate.purejavaendpoint.PureJavaEndpoint.pureJavaEndpointFor;
import static java.util.concurrent.TimeUnit.HOURS;

public final class CookiesExample {

    public static void main(final String[] args) {
        final HttpMate httpMate = anHttpMate()
                .get("/set", (request, response) -> response.setCookie("myCookie", "foo"))
                .get("/setWithOptions", (request, response) -> response.setCookie(cookie("myCookie", "foo").withMaxAge(2, HOURS)))
                .get("/get", (request, response) -> {
                    final String myCookie = request.cookies().getCookie("myCookie");
                    response.setBody("Value was: " + myCookie);
                })
                .get("/invalidate", (request, response) -> response.invalidateCookie("myCookie"))
                .build();
        pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
    }
}
