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

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import static com.envimate.httpmate.tests.givenwhenthen.Given.givenAHttpServer;
import static com.envimate.httpmate.tests.givenwhenthen.Given.givenAnOpenSocketThatCanInterpretHttpValues;

public final class ClientTests {
    private static final String uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
    private static final String numbers = "0123456789";
    private static final String specialCharacters = "/-._~!$&'()*+,;=:@";
    private static final String escapeCharacter = "%";

    private static final String charactersThatNeedEncoding = "[]{}\"§#";
    //private static final String charactersThatNeedEncoding = "[";

    private static final String allowedCharactersInPath = uppercaseLetters + lowercaseLetters + numbers + specialCharacters /*+ escapeCharacter*/;

    @Test
    public void clientDoesNotAppendATrailingSlashToPath() {
        givenAHttpServer()
                .when().aRequestIsMadeToThePath("/qwer")
                .theServerReceivedARequestToThePath("/qwer");
    }

    @Test
    public void clientKeepsAnAppendedTrailingSlashInPath() {
        givenAHttpServer()
                .when().aRequestIsMadeToThePath("/qwer/")
                .theServerReceivedARequestToThePath("/qwer/");
    }

    @Test
    public void clientEncodesPath() {
        givenAHttpServer()
                .when().aRequestIsMadeToThePath("/" + charactersThatNeedEncoding)
                .theServerReceivedARequestToThePath("/%5B%5D%7B%7D%22%C2%A7%23");
    }
}
