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

package com.envimate.httpmate.multipart;

import com.envimate.httpmate.chains.MetaDataKey;

import static com.envimate.httpmate.chains.MetaDataKey.metaDataKey;

public final class MultipartChainKeys {
    public static final MetaDataKey<MultipartIteratorBody> MULTIPART_ITERATOR_BODY = metaDataKey("MULTIPART_ITERATOR_BODY");

    private MultipartChainKeys() {
    }
}
