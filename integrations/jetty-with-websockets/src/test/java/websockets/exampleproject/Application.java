package websockets.exampleproject;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.request.HttpRequestMethod;
import com.envimate.httpmate.websockets.WebSocketModule;
import com.envimate.mapmate.deserialization.Deserializer;
import com.envimate.mapmate.deserialization.methods.DeserializationCPMethod;
import com.envimate.mapmate.serialization.Serializer;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusType;
import com.envimate.messageMate.useCaseAdapter.UseCaseAdapter;
import com.google.gson.Gson;
import websockets.exampleproject.domain.*;
import websockets.exampleproject.usecases.AntiHateSpeechUseCase;
import websockets.exampleproject.usecases.SendMessageRequest;
import websockets.exampleproject.usecases.SendMessageResponse;
import websockets.exampleproject.usecases.SendMessageUseCase;
import websockets.exampleproject.usecases.events.NewMessageEvent;

import java.util.Map;

import static com.envimate.httpmate.HttpMate.aHttpMateDispatchingEventsUsing;
import static com.envimate.httpmate.chains.HttpMateChainKeys.AUTHENTICATION_INFORMATION;
import static com.envimate.httpmate.chains.HttpMateChainKeys.HEADERS;
import static com.envimate.httpmate.multipart.MultipartModule.multipartModule;
import static com.envimate.httpmate.request.ContentType.json;
import static com.envimate.httpmate.unpacking.BodyMapParsingModule.aBodyMapParsingModule;
import static com.envimate.httpmate.websockets.WebSocketModule.webSocketModule;
import static com.envimate.mapmate.deserialization.Deserializer.aDeserializer;
import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterBuilder.anUseCaseAdapter;
import static websockets.exampleproject.CookieParsing.getCookie;

public final class Application {

    public static final MessageBus MESSAGE_BUS = aMessageBus()
            .forType(MessageBusType.ASYNCHRONOUS)
            .withAsynchronousConfiguration(constantPoolSizeAsynchronousPipeConfiguration(4))
            .build();

    public static void startApplication() {

        final Gson gson = new Gson();

        final Deserializer deserializer = aDeserializer()
                .withUnmarshaller(gson::fromJson)
                .withCustomPrimitive(MessageContent.class).deserializedUsingTheStaticMethodWithSingleStringArgument()
                .withCustomPrimitive(Username.class).deserializedUsingTheStaticMethodWithSingleStringArgument()
                .withDataTransferObject(SendMessageRequest.class).deserializedUsingTheSingleFactoryMethod()
                .withCustomPrimitive(User.class).deserializedUsing(new DeserializationCPMethod() {
                    @Override
                    public void verifyCompatibility(Class targetType) {
                    }

                    @Override
                    public Object deserialize(String input, Class targetType) throws Exception {
                        return VERY_STUPID.get();
                    }
                })
                .build();

        final Serializer serializer = Serializer.aSerializer()
                .withMarshaller(gson::toJson)
                .withDataTransferObject(SendMessageResponse.class).serializedByItsPublicFields()
                .withDataTransferObject(Message.class).serializedByItsPublicFields()
                .withDataTransferObject(NewMessageEvent.class).serializedByItsPublicFields()
                .withCustomPrimitive(MessageContent.class).serializedUsingTheMethod(MessageContent::stringValue)
                .withCustomPrimitive(MessageId.class).serializedUsingTheMethod(MessageId::stringValue)
                .withCustomPrimitive(Username.class).serializedUsingTheMethod(Username::stringValue)
                .build();

        final UserRepository userRepository = UserRepository.userRepository();

        final UseCaseAdapter useCaseAdapter = anUseCaseAdapter()
                .invokingUseCase(SendMessageUseCase.class).forType("SendMessageRequest").callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .throwAnExceptionByDefault()
                .throwingAnExceptionIfNoResponseMappingCanBeFound()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault();
        useCaseAdapter.attachTo(MESSAGE_BUS);

        final WebSocketModule webSocketModule = webSocketModule()
                .acceptingWebSocketsToThePath("/connect").saving(AUTHENTICATION_INFORMATION)
                .choosingTheEvent("SendMessageRequest").when(metaData -> true)
                .forwardingTheEvent("NewMessageEvent").toWebSocketsThat((metaData, event) -> {
                    //return event.message.recipients.contains(metaData.get(AUTHENTICATION_INFORMATION));
                    throw new UnsupportedOperationException();
                })
                .closingOn("BanUserEvent").allWebSocketsThat((category, event) -> {
                    //return category.equals(event.username());
                    throw new UnsupportedOperationException();
                })
                .build();

        final HttpMate httpMate = aHttpMateDispatchingEventsUsing(MESSAGE_BUS)
                .choosingTheEvent(EventType.eventTypeFromString("SendMessageRequest")).forRequestPath("/qwrefewiflrwefjierwipower").andRequestMethod(HttpRequestMethod.DELETE)
                //.choosingTheEvent("UPLOAD_USER_AVATAR").forRequestPath("/user/<id>/upload").andRequestMethod(HttpRequestMethod.GET)
                /*
                .mappingRequestsToUseCaseParametersByDefaultUsing(theMapMateDeserializerOnTheRequestBody(deserializer)
                        .andInjectingRequestValuesIntoTheJsonBodyUsing((metaData, json) -> {
                            final User user = (User) metaData.get(AUTHENTICATION_INFORMATION);
                            json.put("sender", "dummy");
                            VERY_STUPID.set(user);
                        }))
                        */
                .preparingRequestsForParameterMappingThatByDirectlyMappingAllData()
                //.mappingResponsesUsing(theMapMateSerializer(serializer)::map)
                .mappingResponsesUsing((event, metaData) -> {
                })
                //.serializingResponseObjectsByDefaultUsing(theMapMateSerializer(serializer))
                .configuredBy(configurator -> {
                    configurator.configureSecurity().addAuthenticator(metaData -> metaData.get(HEADERS).getHeader("cookie").flatMap(
                            cookieHeader -> getCookie("username", cookieHeader).flatMap(
                                    username -> getCookie("password", cookieHeader).flatMap(
                                            password -> userRepository.getIfCorrectAuthenticationInformation(username, password)))));
                    configurator.configureSecurity().addAuthorizer(metaData -> metaData.getOptional(AUTHENTICATION_INFORMATION).isPresent());
                    configurator.configureLogger().loggingToStderr();
                    configurator.registerModule(webSocketModule);
                    configurator.registerModule(multipartModule());
                    configurator.registerModule(aBodyMapParsingModule()
                            .parsingContentType(json()).with(body -> new Gson().fromJson(body, Map.class))
                            .usingTheDefaultContentType(json()));
                });

        final String chains = httpMate.dumpChains();
        System.out.println("chains = " + chains);

        //final JettyEndpoint jettyEndpoint = jettyEndpointFor(doubleServletFor(httpMate)).listeningOnThePort(8976);

        AntiHateSpeechUseCase.register();
    }

    private static final ThreadLocal<User> VERY_STUPID = new ThreadLocal<>();

    public static void main(String[] args) {
        startApplication();
    }
}
