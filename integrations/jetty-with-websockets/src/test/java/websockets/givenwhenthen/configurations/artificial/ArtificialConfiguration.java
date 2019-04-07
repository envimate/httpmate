package websockets.givenwhenthen.configurations.artificial;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.request.HttpRequestMethod;
import com.envimate.httpmate.websockets.WebSocketModule;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusType;
import com.envimate.messageMate.useCaseAdapter.UseCaseAdapter;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import websockets.givenwhenthen.configurations.TestConfiguration;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseA;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseB;
import websockets.givenwhenthen.configurations.artificial.usecases.abc.UseCaseC;
import websockets.givenwhenthen.configurations.artificial.usecases.both.BothUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.close.CloseUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.count.CountUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.echo.EchoParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.echo.EchoUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.exception.ExceptionUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.exception.ExceptionUseCaseParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.headers.HeaderParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.headers.HeaderUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.normal.NormalUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.pathparameter.ParameterParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.pathparameter.ParameterUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.query.QueryParameter;
import websockets.givenwhenthen.configurations.artificial.usecases.query.QueryUseCase;
import websockets.givenwhenthen.configurations.artificial.usecases.queryfoo.QueryFooUseCase;

import java.util.Map;

import static com.envimate.httpmate.HttpMate.aHttpMateDispatchingEventsUsing;
import static com.envimate.httpmate.chains.HttpMateChainKeys.*;
import static com.envimate.httpmate.request.ContentType.json;
import static com.envimate.httpmate.unpacking.BodyMapParsingModule.aBodyMapParsingModule;
import static com.envimate.httpmate.websockets.WebSocketModule.webSocketModule;
import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.messageBus.EventType.eventTypeFromString;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterBuilder.anUseCaseAdapter;
import static websockets.givenwhenthen.configurations.TestConfiguration.testConfiguration;
import static websockets.givenwhenthen.configurations.artificial.usecases.echo.EchoParameter.echoParameter;
import static websockets.givenwhenthen.configurations.artificial.usecases.exception.ExceptionUseCaseParameter.exceptionUseCaseParameter;
import static websockets.givenwhenthen.configurations.artificial.usecases.headers.HeaderParameter.headerParameter;
import static websockets.givenwhenthen.configurations.artificial.usecases.pathparameter.ParameterParameter.parameterParameter;
import static websockets.givenwhenthen.configurations.artificial.usecases.query.QueryParameter.queryParameter;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArtificialConfiguration {

    public static volatile MessageBus MESSAGE_BUS;

    public static TestConfiguration theExampleHttpMateInstanceWithWebSocketsSupport() {
        MESSAGE_BUS = aMessageBus()
                .forType(MessageBusType.ASYNCHRONOUS)
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousPipeConfiguration(4))
                .build();
        final UseCaseAdapter useCaseAdapter = anUseCaseAdapter()
                .invokingUseCase(NormalUseCase.class).forType("NormalUseCase").callingTheSingleUseCaseMethod()
                .invokingUseCase(BothUseCase.class).forType("BothUseCase").callingTheSingleUseCaseMethod()
                .invokingUseCase(CountUseCase.class).forType("CountUseCase").callingTheSingleUseCaseMethod()
                .invokingUseCase(CloseUseCase.class).forType("CloseUseCase").callingTheSingleUseCaseMethod()
                .invokingUseCase(QueryFooUseCase.class).forType("QueryFooUseCase").callingTheSingleUseCaseMethod()
                .invokingUseCase(UseCaseA.class).forType("UseCaseA").callingTheSingleUseCaseMethod()
                .invokingUseCase(UseCaseB.class).forType("UseCaseB").callingTheSingleUseCaseMethod()
                .invokingUseCase(UseCaseC.class).forType("UseCaseC").callingTheSingleUseCaseMethod()
                .invokingUseCase(ExceptionUseCase.class).forType("ExceptionUseCaseParameter").callingTheSingleUseCaseMethod()
                .invokingUseCase(QueryUseCase.class).forType("QueryParameter").callingTheSingleUseCaseMethod()
                .invokingUseCase(HeaderUseCase.class).forType("HeaderParameter").callingTheSingleUseCaseMethod()
                .invokingUseCase(ParameterUseCase.class).forType("ParameterParameter").callingTheSingleUseCaseMethod()
                .invokingUseCase(EchoUseCase.class).forType("EchoParameter").callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .mappingRequestsToUseCaseParametersOfType(QueryParameter.class).using((targetType, map) -> queryParameter((String) map.get("var")))
                .mappingRequestsToUseCaseParametersOfType(HeaderParameter.class).using((targetType, map) -> headerParameter((String) map.get("var")))
                .mappingRequestsToUseCaseParametersOfType(ParameterParameter.class).using((targetType, map) -> parameterParameter((String) map.get("var")))
                .mappingRequestsToUseCaseParametersOfType(EchoParameter.class).using((targetType, map) -> echoParameter((String) map.get("echoValue")))
                .mappingRequestsToUseCaseParametersOfType(ExceptionUseCaseParameter.class).using((targetType, map) -> exceptionUseCaseParameter((String) map.get("mode")))
                .throwAnExceptionByDefault()
                .serializingResponseObjectsOfType(String.class).using(object -> Map.of("stringValue", object))
                .throwingAnExceptionIfNoResponseMappingCanBeFound()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault();

        useCaseAdapter.attachTo(MESSAGE_BUS);

        final WebSocketModule webSocketModule = webSocketModule()
                .acceptingWebSocketsToThePath("/").taggedBy("ROOT")
                .acceptingWebSocketsToThePath("/close").taggedBy("CLOSE")
                .acceptingWebSocketsToThePath("/both").taggedBy("BOTH")
                .acceptingWebSocketsToThePath("/authorized").taggedBy("AUTHORIZED")
                .acceptingWebSocketsToThePath("/count").taggedBy("COUNT")
                .acceptingWebSocketsToThePath("/query_foo").taggedBy("QUERY_FOO")
                .acceptingWebSocketsToThePath("/echo").taggedBy("ECHO")
                .acceptingWebSocketsToThePath("/pre/<var>/post").taggedBy("PARAMETERIZED")
                .acceptingWebSocketsToThePath("/query").taggedBy("QUERY")
                .acceptingWebSocketsToThePath("/header").taggedBy("HEADER")
                .acceptingWebSocketsToThePath("/exception").taggedBy("EXCEPTION")
                .choosingTheEvent("CloseUseCase").forWebSocketsTaggedWith("CLOSE")
                .choosingTheEvent("CountUseCase").forWebSocketsTaggedWith("COUNT")
                .choosingTheEvent("UseCaseA").when(metaData -> metaData.get(BODY_MAP).getOrDefault("useCase", "").equals("A"))
                .choosingTheEvent("UseCaseB").when(metaData -> metaData.get(BODY_MAP).getOrDefault("useCase", "").equals("B"))
                .choosingTheEvent("UseCaseC").when(metaData -> metaData.get(BODY_MAP).getOrDefault("useCase", "").equals("C"))
                .choosingTheEvent("QueryFooUseCase").forWebSocketsTaggedWith("QUERY_FOO")
                .choosingTheEvent("ExceptionUseCaseParameter").forWebSocketsTaggedWith("EXCEPTION")
                .choosingTheEvent("EchoParameter").forWebSocketsTaggedWith("ECHO")
                .choosingTheEvent("ParameterParameter").forWebSocketsTaggedWith("PARAMETERIZED")
                .choosingTheEvent("QueryParameter").forWebSocketsTaggedWith("QUERY")
                .choosingTheEvent("HeaderParameter").forWebSocketsTaggedWith("HEADER")
                .closingOn("CloseEvent").allWebSockets()
                .build();

        final HttpMate httpMate = aHttpMateDispatchingEventsUsing(MESSAGE_BUS)
                .choosingTheEvent(eventTypeFromString("NormalUseCase")).forRequestPath("/normal").andRequestMethod(HttpRequestMethod.GET)
                .choosingTheEvent(eventTypeFromString("BothUseCase")).forRequestPath("/both").andRequestMethod(HttpRequestMethod.GET)
                .preparingRequestsForParameterMappingThatByDirectlyMappingAllData()
                .mappingResponsesUsing((event, metaData) -> metaData.set(STRING_RESPONSE, event.toString()))
                .configuredBy(configurator -> {
                    configurator.configureSecurity().addAuthenticator(metaData -> metaData.get(QUERY_PARAMETERS).getQueryParameter("username"));
                    configurator.configureSecurity().addAuthenticator(metaData -> metaData.get(HEADERS).getHeader("username"));
                    configurator.configureSecurity().addAuthorizer(metaData -> {
                        final String path = metaData.get(PATH);
                        if ("/authorized".equals(path)) {
                            return metaData.getOptional(AUTHENTICATION_INFORMATION)
                                    .map("admin"::equals)
                                    .orElse(false);
                        }
                        return true;
                    });
                    configurator.configureLogger().loggingToStderr();
                    configurator.registerModule(webSocketModule);
                    configurator.registerModule(aBodyMapParsingModule()
                            .parsingContentType(json()).with(body -> new Gson().fromJson(body, Map.class))
                            .usingTheDefaultContentType(json()));
                });

        return testConfiguration(httpMate, webSocketModule);
    }
}
