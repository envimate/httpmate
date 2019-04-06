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

package com.envimate.httpmate.tests.givenwhenthen.client;

import com.envimate.httpmate.tests.givenwhenthen.builders.MultipartElement;

import java.util.List;
import java.util.Map;

public interface HttpClientWrapper {
    HttpClientResponse issueRequestWithoutBody(String path, String method, Map<String, String> headers);
    HttpClientResponse issueRequestWithStringBody(String path, String method, Map<String, String> headers, String body);
    HttpClientResponse issueRequestWithMultipartBody(String path, String method, Map<String, String> headers, List<MultipartElement> parts);
}
