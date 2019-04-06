package websockets;

import org.junit.Ignore;
import org.junit.Test;

import static websockets.givenwhenthen.Given.givenTheExampleChatServer;
import static websockets.givenwhenthen.Given.givenTheExampleHttpMateInstanceWithWebSocketSupport;

public final class WebSocketsSpecs {

    @Test
    public void testAWebSocketCanConnectToHttpMate() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/").withoutHeaders()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully();
    }

    @Test
    public void testAWebSocketCannotConnectToAPathNotSpecifiedForWebSockets() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/invalid").withoutHeaders()
                .then().theWebSocketConnectionCouldNotBeEstablished();
    }

    @Test
    public void testARequestToANormalRouteStillWorks() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aNormalGETRequestIsIssuedToThePath("/normal").withoutHeaders()
                .then().theResponseBodyWas("{stringValue=just a normal response}");
    }

    @Test
    public void testAWebSocketCannotConnectToAPathOnlySpecifiedForNormalRequests() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/normal").withoutHeaders()
                .then().theWebSocketConnectionCouldNotBeEstablished();
    }

    @Test
    public void testAWebSocketCanConnectToAPathThatIsSpecifiedForNormalAndWebSocketRequests() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/both").withoutHeaders()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully();
    }

    @Test
    public void testANormalRequestBeIssuedToAPathThatIsSpecifiedForNormalAndWebSocketRequests() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aNormalGETRequestIsIssuedToThePath("/both").withoutHeaders()
                .then().theResponseBodyWas("{stringValue=this is both}");
    }

    @Test
    public void testAWebSocketCannotConnectUnauthorizedIfAuthorizationIsRequired() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/authorized").withoutHeaders()
                .then().theWebSocketConnectionCouldNotBeEstablished();
    }

    @Test
    public void testAWebSocketCanBeAuthenticatedByQueryParameters() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/authorized?username=admin").withoutHeaders()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully();
    }

    @Test
    public void testAWebSocketCanBeAuthenticatedByHeaderValues() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/authorized").withTheHeaders("username=admin")
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully();
    }

    @Test
    public void testTheFramesOfAWebSocketAreForwardedToAUseCase() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/count").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedUntilTheCounterOfTheCountUseCaseReaches(1)
                .then().theCounterOfTheCountUseCaseWas(1);
    }

    @Test
    public void testMultipleFramesOfAWebsocketCanBeForwardedToAUseCase() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/count").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedUntilTheCounterOfTheCountUseCaseReaches(3)
                .then().theCounterOfTheCountUseCaseWas(3);
    }

    @Test
    public void testOneWebSocketCanSendMessagesToMulipleUseCases() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("{ \"useCase\": \"A\" }")
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("{ \"useCase\": \"B\" }")
                .andWhen().itIsWaitedUntilUseCaseAHasBeenInvoked()
                .andWhen().itIsWaitedUntilUseCaseBHasBeenInvoked()
                .then()
                .useCaseAHasBeenInvoked()
                .useCaseBHasBeenInvoked()
                .useCaseCHasNotBeenInvoked();
    }

    @Test
    public void testAUseCaseCanRespondViaTheWebSocket() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/query_foo").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().exactlyOneWebSocketReceivedMessage("{stringValue=foo}");
    }

    @Test
    public void testTheContentOfAFrameCanGetMappedToAUseCaseParameter() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/echo").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("{ \"echoValue\": \"test\" }")
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().exactlyOneWebSocketReceivedMessage("{stringValue=test}");
    }

    @Test
    public void testAWebSocketCanConnectToAParameterizedPath() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/pre/foo/post").withoutHeaders()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully();
    }

    @Test
    public void testPathParametersCanBeMappedToUseCaseParameters() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/pre/yxcv/post").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().exactlyOneWebSocketReceivedMessage("{stringValue=yxcv}");
    }

    @Test
    public void testQueryParametersCanBeMappedToUseCaseParameters() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/query?var=hooo").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().exactlyOneWebSocketReceivedMessage("{stringValue=hooo}");
    }

    @Test
    public void testHeadersCanBeMappedToUseCaseParameters() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/header").withTheHeaders("var=mmm")
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().exactlyOneWebSocketReceivedMessage("{stringValue=mmm}");
    }

    @Test
    public void testAWebSocketCanReceiveArbitraryMessagesFromUseCases() {
        givenTheExampleChatServer()
                .when().aWebSocketIsConnectedToThePath("/subscribe").withTheHeaders("user=elefant")
                .andWhen().aNormalGETRequestIsIssuedToThePath("/send").withTheHeaders("user=maus", "content=hallo", "recipient=elefant")
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully()
                .exactlyOneWebSocketReceivedMessage("hallo");
    }

    @Test
    public void testAMultiBrowsertabAwareChatServerCanBeImplemented() {
        givenTheExampleChatServer()
                .when().aWebSocketIsConnectedToThePath("/subscribe").withTheHeaders("user=elefant")
                .andWhen().aWebSocketIsConnectedToThePath("/subscribe").withTheHeaders("user=elefant")
                .andWhen().aNormalGETRequestIsIssuedToThePath("/send").withTheHeaders("user=maus", "content=hallo", "recipient=elefant")
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully()
                .exactlyTwoDifferentWebSocketsReceivedTheMessage("hallo");
    }

    @Test
    public void testASendingUseCaseCanDistinguishBetweenDifferentGroupsOfWebSockets() {
        givenTheExampleChatServer()
                .when().aWebSocketIsConnectedToThePath("/subscribe").withTheHeaders("user=elefant")
                .andWhen().aWebSocketIsConnectedToThePath("/subscribe").withTheHeaders("user=ente")
                .andWhen().aNormalGETRequestIsIssuedToThePath("/send").withTheHeaders("user=maus", "content=die ente ist doof", "recipient=elefant")
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully()
                .exactlyOneWebSocketReceivedMessage("die ente ist doof");
    }

    @Test
    public void testTheNumberOfActiveConnectionsCanBeQueried() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/").withoutHeaders()
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(1);
    }

    @Test
    public void testAWebSocketThatGetsClosedByTheClientWillGetCleanedUp() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/").withoutHeaders()
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(1)
                .andWhen().allWebSocketsAreClosedOnClientSide()
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(0)
                .exactlyOneClientHasBeenClosed();
    }

    @Test
    public void testAWebSocketThatGetsClosedByTheServerWillGetCleanedUp() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/close").withoutHeaders()
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(1)
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                //.andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("{ close all websockets }")
                .andWhen().itItWaitedForTheClosingOfAWebSocket()
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(0)
                .exactlyOneClientHasBeenClosed();
    }

    @Test
    public void testAnExceptionDuringWebSocketMessageProcessingDoesNotCloseTheWebSocket() {
        givenTheExampleHttpMateInstanceWithWebSocketSupport()
                .when().aWebSocketIsConnectedToThePath("/exception").withoutHeaders()
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(1)
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("{ \"mode\": \"throw\"}")
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(1)
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("{ \"mode\": \"hello\"}")
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().exactlyOneWebSocketReceivedMessage("{stringValue=hello}");
    }
}
