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

package com.envimate.httpmate.tests.multipart;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.multipart.MultipartIteratorBody;
import com.envimate.httpmate.multipart.MultipartPart;
import com.envimate.httpmate.path.Path;
import com.envimate.httpmate.security.NotAuthorizedException;

import static com.envimate.httpmate.HttpMate.aLowLevelHttpMate;
import static com.envimate.httpmate.HttpMateChainKeys.*;
import static com.envimate.httpmate.convenience.configurators.exceptions.ExceptionMappingConfigurator.toMapExceptions;
import static com.envimate.httpmate.exceptions.DefaultExceptionMapper.theDefaultExceptionMapper;
import static com.envimate.httpmate.http.Http.StatusCodes.FORBIDDEN;
import static com.envimate.httpmate.http.HttpRequestMethod.*;
import static com.envimate.httpmate.multipart.MultipartChainKeys.MULTIPART_ITERATOR_BODY;
import static com.envimate.httpmate.multipart.MultipartModule.toExposeMultipartBodiesUsingMultipartIteratorBody;
import static com.envimate.httpmate.security.SecurityConfigurators.toAuthenticateRequests;
import static com.envimate.httpmate.security.SecurityConfigurators.toAuthorizeRequests;
import static com.envimate.httpmate.tests.Util.extractUsername;
import static com.envimate.httpmate.tests.multipart.AuthenticatedHandler.authenticatedHandler;
import static com.envimate.httpmate.tests.multipart.AuthorizedHandler.authorizedHandler;
import static com.envimate.httpmate.tests.multipart.DumpMultipartBodyHandler.dumpMultipartBodyHandler;
import static java.util.Optional.empty;

public final class MultipartHttpMateConfiguration {

    private MultipartHttpMateConfiguration() {
    }

    public static HttpMate theMultipartHttpMateInstanceUsedForTesting() {
        return aLowLevelHttpMate()
                .callingTheHandler(dumpMultipartBodyHandler())
                .forRequestPath("/dump").andRequestMethods(GET, POST, PUT, DELETE)
                .callingTheHandler(authenticatedHandler())
                .forRequestPath("/authenticated").andRequestMethods(GET, POST, PUT, DELETE)
                .callingTheHandler(authorizedHandler())
                .forRequestPath("/authorized").andRequestMethods(GET, POST, PUT, DELETE)
                .thatIs()
                .configured(toAuthenticateRequests().afterBodyProcessing().using(metaData -> {
                    final Path path = metaData.get(PATH);
                    if (path.matches("/authenticated") || path.matches("/authorized")) {
                        final MultipartIteratorBody multipartIteratorBody = metaData.get(MULTIPART_ITERATOR_BODY);
                        final MultipartPart firstPart = multipartIteratorBody.next("authentication");
                        final String content = firstPart.readContentToString();
                        return extractUsername(content);
                    }
                    return empty();
                }))
                .configured(toAuthorizeRequests().afterBodyProcessing().using(metaData -> {
                    if (metaData.get(PATH).matches("/authorized")) {
                        return metaData.get(AUTHENTICATION_INFORMATION).equals("admin");
                    } else {
                        return true;
                    }
                }))
                .configured(toMapExceptions()
                        .ofType(NotAuthorizedException.class).toResponsesUsing((e, metaData) -> {
                            metaData.set(RESPONSE_STATUS, FORBIDDEN);
                            metaData.set(RESPONSE_STRING, "Access denied!");
                        })
                        .ofAllRemainingTypesUsing(theDefaultExceptionMapper()))
                .configured(toExposeMultipartBodiesUsingMultipartIteratorBody())
                .build();
    }
}
