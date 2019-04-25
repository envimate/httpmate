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

package websockets.givenwhenthen;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseA;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseB;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseC;
import websockets.givenwhenthen.configurations.artificial.usecases.count.CountUseCase;

import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static websockets.givenwhenthen.configurations.lowlevel.LowLevelConfiguration.logger;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Then {
    private final ReportBuilder reportBuilder;
    private final When when;

    static Then then(final ReportBuilder reportBuilder,
                     final When when) {
        return new Then(reportBuilder, when);
    }

    public Then allWebSocketConnectionsCouldBeEstablishedSuccessfully() {
        assertThat(reportBuilder.getExceptionsDuringWebSocketConnecting().isEmpty(), is(true));
        return this;
    }

    public Then theWebSocketConnectionCouldNotBeEstablished() {
        assertThat(reportBuilder.getExceptionsDuringWebSocketConnecting().isEmpty(), is(false));
        return this;
    }

    public Then theResponseBodyWas(final String content) {
        assertThat(reportBuilder.getNormalResponseBody(), is(content));
        return this;
    }

    public Then theCounterOfTheCountUseCaseWas(final int expectedCount) {
        assertThat(CountUseCase.COUNTER.getValue(), is(expectedCount));
        return this;
    }

    public Then useCaseAHasBeenInvoked() {
        assertThat(UseCaseA.HAS_BEEN_INVOKED.getValue(), is(true));
        return this;
    }

    public Then useCaseBHasBeenInvoked() {
        assertThat(UseCaseB.HAS_BEEN_INVOKED.getValue(), is(true));
        return this;
    }

    public Then useCaseCHasNotBeenInvoked() {
        assertThat(UseCaseC.HAS_BEEN_INVOKED.getValue(), is(false));
        return this;
    }

    public Then exactlyOneClientHasBeenClosed() {
        assertThat(numberOfDifferentWebSocketsThatHaveBeenReportedAsClosedOnClientSide(), is(1L));
        return this;
    }

    public Then exactlyOneWebSocketReceivedMessage(final String expectedReceivedMessage) {
        return exactlyNDifferentWebSocketsReceivedTheMessage(1, expectedReceivedMessage);
    }

    public Then exactlyTwoDifferentWebSocketsReceivedTheMessage(final String expectedReceivedMessage) {
        return exactlyNDifferentWebSocketsReceivedTheMessage(2, expectedReceivedMessage);
    }

    public Then theQueriedNumberOfActiveConnectionsWas(final int expected) {
        assertThat(reportBuilder.getNumberOfActiveWebSockets(), is(expected));
        return this;
    }

    private Then exactlyNDifferentWebSocketsReceivedTheMessage(final long n, final String expectedReceivedMessage) {
        assertThat(numberOfDifferentWebSocketsThatReceived(expectedReceivedMessage), is(n));
        return this;
    }

    private long numberOfDifferentWebSocketsThatReceived(final String expectedReceivedMessage) {
        return numberOfDifferentWebSocketsThat(report -> report.didReceive(expectedReceivedMessage));
    }

    private long numberOfDifferentWebSocketsThatHaveBeenReportedAsClosedOnClientSide() {
        return numberOfDifferentWebSocketsThat(SingleWebSocketReportBuilder::wasClosed);
    }

    private long numberOfDifferentWebSocketsThat(final Predicate<SingleWebSocketReportBuilder> predicate) {
        return reportBuilder.getWebSocketReporters().stream()
                .filter(predicate)
                .count();
    }

    public Then theLogOutputStartedWith(final String expectedPrefix) {
        final String logContent = logger.toString();
        assertThat(logContent, startsWith(expectedPrefix));
        return this;
    }

    public When andWhen() {
        return when;
    }
}
