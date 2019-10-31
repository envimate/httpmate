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

package com.envimate.httpmate.http.headers.cookies;

import com.envimate.httpmate.util.Validators;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import static com.envimate.httpmate.http.headers.cookies.CookieName.cookieName;
import static com.envimate.httpmate.http.headers.cookies.CookieValue.cookieValue;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CookieBuilder {
    private final DateTimeFormatter httpDateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O");

    private final CookieName cookieName;
    private final CookieValue cookieValue;

    private volatile Instant expires;
    private volatile Long maxAgeInSeconds;
    private volatile boolean secure = false;
    private volatile boolean httpOnly = false;
    private volatile SameSitePolicy sameSitePolicy;
    private volatile List<String> domainScope;
    private volatile List<String> pathScope;

    public static CookieBuilder cookie(final String name, final String value) {
        final CookieName cookieName = cookieName(name);
        final CookieValue cookieValue = cookieValue(value);
        return new CookieBuilder(cookieName, cookieValue);
    }

    public CookieBuilder withExpiration(final Instant expiration) {
        Validators.validateNotNull(expiration, "expiration");
        this.expires = expiration;
        return this;
    }

    public CookieBuilder withMaxAge(final int time, final TimeUnit unit) {
        validateNotNull(unit, "unit");
        maxAgeInSeconds = unit.toSeconds(time);
        return this;
    }

    public CookieBuilder thatIsOnlySentViaHttps() {
        this.secure = true;
        return this;
    }

    public CookieBuilder thatIsNotAccessibleFromJavaScript() {
        this.httpOnly = true;
        return this;
    }

    public CookieBuilder withSameSitePolicy(final SameSitePolicy policy) {
        validateNotNull(policy, "policy");
        this.sameSitePolicy = policy;
        return this;
    }

    public CookieBuilder exposedToAllSubdomainsOf(final String... domains) {
        this.domainScope = asList(domains);
        return this;
    }

    public CookieBuilder exposedOnlyToSubpathsOf(final String... paths) {
        this.pathScope = asList(paths);
        return this;
    }

    public String build() {
        final StringJoiner joiner = new StringJoiner("; ");
        final String nameAndValue = format("%s=\"%s\"", cookieName.stringValue(), cookieValue.stringValue());
        joiner.add(nameAndValue);

        if (nonNull(expires)) {
            final ZonedDateTime zonedDateTime = expires.atZone(ZoneId.of("GMT"));
            final String formattedDate = httpDateTimeFormatter.format(zonedDateTime);
            joiner.add(format("Expires=%s", formattedDate));
        }

        if (nonNull(maxAgeInSeconds)) {
            joiner.add(format("Max-Age=%d", maxAgeInSeconds));
        }

        if (nonNull(domainScope)) {
            final String domains = join(",", domainScope);
            joiner.add(format("Domain=%s", domains));
        }

        if (nonNull(pathScope)) {
            final String paths = join(",", pathScope);
            joiner.add(format("Path=%s", paths));
        }

        if (secure) {
            joiner.add("Secure");
        }

        if (httpOnly) {
            joiner.add("HttpOnly");
        }

        if (nonNull(sameSitePolicy)) {
            joiner.add(format("SameSite=%s", sameSitePolicy.stringValue()));
        }

        return joiner.toString();
    }
}
