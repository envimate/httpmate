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

package com.envimate.httpmate.websockets;

import com.envimate.httpmate.chains.MetaData;
import com.envimate.httpmate.chains.MetaDataKey;

import java.util.function.Function;

import static com.envimate.httpmate.websockets.MetaDataEntry.metaDataEntry;

public interface MetaDataEntryProvider<T> {

    static <T> MetaDataEntryProvider<T> saving(final MetaDataKey<T> key) {
        return metaData -> metaDataEntry(key, metaData.get(key));
    }

    static <T> MetaDataEntryProvider<T> storing(final MetaDataKey<T> key, Function<MetaData, T> valueProvider) {
        return metaData -> metaDataEntry(key, valueProvider.apply(metaData));
    }

    MetaDataEntry<T> provide(MetaData metaData);
}
