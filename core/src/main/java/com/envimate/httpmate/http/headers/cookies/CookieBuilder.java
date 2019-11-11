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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.envimate.httpmate.http.headers.cookies.CookieName.cookieName;
import static com.envimate.httpmate.http.headers.cookies.CookieValue.cookieValue;
import static com.envimate.httpmate.util.Validators.validateNotNull;
import static com.envimate.httpmate.util.Validators.validateNotNullNorEmpty;
import static java.lang.String.*;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static java.util.Locale.US;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CookieBuilder {
    private final DateTimeFormatter httpDateTimeFormatter = ofPattern("EEE, dd MMM yyyy HH:mm:ss O").localizedBy(US);

    private final List<String> elements;

    public static CookieBuilder cookie(final String name, final String value) {
        final CookieName cookieName = cookieName(name);
        final CookieValue cookieValue = cookieValue(value);
        final String nameAndValue = format("%s=\"%s\"", cookieName.stringValue(), cookieValue.stringValue());
        final List<String> elements = new LinkedList<>();
        elements.add(nameAndValue);
        return new CookieBuilder(elements);
    }

    public CookieBuilder withDirective(final String directive) {
        validateNotNullNorEmpty(directive, "directive");
        elements.add(directive);
        return this;
    }

    public CookieBuilder withDirective(final String key, final String value) {
        validateNotNullNorEmpty(key, "key");
        validateNotNullNorEmpty(value, "value");
        return withDirective(format("%s=%s", key, value));
    }

    public CookieBuilder withExpiresDirective(final String expiresDirective) {
        return withDirective("Expires", expiresDirective);
    }

    public CookieBuilder withExpiration(final Instant expiration) {
        Validators.validateNotNull(expiration, "expiration");
        final ZonedDateTime zonedDateTime = expiration.atZone(ZoneId.of("GMT"));
        final String formattedDate = httpDateTimeFormatter.format(zonedDateTime);
        return withExpiresDirective(formattedDate);
    }

    public CookieBuilder withMaxAgeDirective(final String maxAgeDirective) {
        return withDirective("Max-Age", maxAgeDirective);
    }

    public CookieBuilder withMaxAge(final int time, final TimeUnit unit) {
        validateNotNull(unit, "unit");
        final long maxAgeInSeconds = unit.toSeconds(time);
        return withMaxAgeDirective(valueOf(maxAgeInSeconds));
    }

    public CookieBuilder withSecureDirective() {
        return withDirective("Secure");
    }

    public CookieBuilder thatIsOnlySentViaHttps() {
        return withSecureDirective();
    }

    public CookieBuilder withHttpOnlyDirective() {
        return withDirective("HttpOnly");
    }

    public CookieBuilder thatIsNotAccessibleFromJavaScript() {
        return withHttpOnlyDirective();
    }

    public CookieBuilder withSameSiteDirective(final String sameSiteDirective) {
        return withDirective("SameSite", sameSiteDirective);
    }

    public CookieBuilder withSameSitePolicy(final SameSitePolicy policy) {
        validateNotNull(policy, "policy");
        return withSameSiteDirective(policy.stringValue());
    }

    public CookieBuilder withDomainDirective(final String domainDirective) {
        return withDirective("Domain", domainDirective);
    }

    public CookieBuilder exposedToAllSubdomainsOf(final String... domains) {
        return withDomainDirective(join(",", asList(domains)));
    }

    public CookieBuilder withPathDirective(final String pathDirective) {
        return withDirective("Path", pathDirective);
    }

    public CookieBuilder exposedOnlyToSubpathsOf(final String... paths) {
        return withPathDirective(join(",", asList(paths)));
    }

    public String build() {
        return join("; ", elements);
    }
}
