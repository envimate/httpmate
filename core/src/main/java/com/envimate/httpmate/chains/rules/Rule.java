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

package com.envimate.httpmate.chains.rules;

import com.envimate.httpmate.chains.Chain;
import com.envimate.httpmate.chains.MetaData;

import java.util.function.Predicate;

import static com.envimate.httpmate.chains.rules.Jump.jumpTo;

/**
 * Routing rules are different from Processors. There shall only be one rule resolved for one request, with
 * the exception, of a chains default rule, which will be used, in case no specific Rule matches the request.
 */
public interface Rule {

    static Rule jumpRule(final Chain targetChain, final Predicate<MetaData> matcher) {
        return new Rule() {
            @Override
            public boolean matches(final MetaData metaData) {
                return matcher.test(metaData);
            }

            @Override
            public Action action() {
                return jumpTo(targetChain);
            }
        };
    }

    boolean matches(MetaData metaData);

    Action action();
}
