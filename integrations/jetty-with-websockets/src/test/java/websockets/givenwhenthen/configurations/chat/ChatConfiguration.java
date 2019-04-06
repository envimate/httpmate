package websockets.givenwhenthen.configurations.chat;

import com.envimate.httpmate.HttpMate;
import com.envimate.httpmate.convenience.preprocessors.Authenticator;
import com.envimate.httpmate.convenience.preprocessors.Authorizer;
import com.envimate.httpmate.websockets.WebSocketModule;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusType;
import com.envimate.messageMate.useCaseAdapter.UseCaseAdapter;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import websockets.givenwhenthen.configurations.TestConfiguration;
import websockets.givenwhenthen.configurations.chat.domain.User;
import websockets.givenwhenthen.configurations.chat.domain.UserRepository;
import websockets.givenwhenthen.configurations.chat.domain.Username;
import websockets.givenwhenthen.configurations.chat.usecases.ChatMessage;
import websockets.givenwhenthen.configurations.chat.usecases.SendMessageUseCase;

import java.util.Map;
import java.util.Objects;

import static com.envimate.httpmate.HttpMate.aHttpMateDispatchingEventsUsing;
import static com.envimate.httpmate.chains.HttpMateChainKeys.*;
import static com.envimate.httpmate.request.ContentType.json;
import static com.envimate.httpmate.request.HttpRequestMethod.GET;
import static com.envimate.httpmate.unpacking.BodyMapParsingModule.aBodyMapParsingModule;
import static com.envimate.httpmate.websockets.WebSocketModule.webSocketModule;
import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.messageBus.EventType.eventTypeFromString;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterBuilder.anUseCaseAdapter;
import static websockets.givenwhenthen.configurations.TestConfiguration.testConfiguration;
import static websockets.givenwhenthen.configurations.chat.domain.MessageContent.messageContent;
import static websockets.givenwhenthen.configurations.chat.domain.UserRepository.userRepository;
import static websockets.givenwhenthen.configurations.chat.domain.Username.username;
import static websockets.givenwhenthen.configurations.chat.usecases.ChatMessage.chatMessage;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatConfiguration {

    public volatile static MessageBus MESSAGE_BUS;

    public static TestConfiguration theExampleChatServerHttpMateInstance() {
        MESSAGE_BUS = aMessageBus()
                .forType(MessageBusType.ASYNCHRONOUS)
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousPipeConfiguration(4))
                .build();

        final UserRepository userRepository = userRepository();
        final Authenticator authenticator = metaData -> metaData.get(HEADERS)
                .getHeader("user")
                .map(Username::username)
                .map(userRepository::byName);
        final Authorizer authorizer = metaData -> metaData.getOptional(AUTHENTICATION_INFORMATION).isPresent();

        final UseCaseAdapter useCaseAdapter = anUseCaseAdapter()
                .invokingUseCase(SendMessageUseCase.class).forType("ChatMessage")
                .callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .mappingRequestsToUseCaseParametersOfType(ChatMessage.class).using((type, map) -> {
                    final String content = (String) map.get("content");
                    final String recipient = (String) map.get("recipient");
                    return chatMessage(messageContent(content), username(recipient));
                })
                .throwAnExceptionByDefault()
                .serializingResponseObjectsThat(Objects::isNull).using(object -> null)
                .throwingAnExceptionIfNoResponseMappingCanBeFound()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault();

        useCaseAdapter.attachTo(MESSAGE_BUS);

        final WebSocketModule webSocketModule = webSocketModule()
                .acceptingWebSocketsToThePath("/subscribe").saving(AUTHENTICATION_INFORMATION)
                .forwardingTheEvent("NewMessageEvent").toWebSocketsThat((metaData, event) -> {
                    final Map<String, Object> map = (Map<String, Object>) event;
                    final String username = metaData.getAs(AUTHENTICATION_INFORMATION, User.class).name().internalValueForMapping();
                    return Objects.equals(map.get("recipient"), username);
                })
                .build();

        final HttpMate httpMate = aHttpMateDispatchingEventsUsing(MESSAGE_BUS)
                .choosingTheEvent(eventTypeFromString("ChatMessage")).forRequestPath("/send").andRequestMethod(GET)
                .mappingRequestsToEventByDirectlyMappingAllData()
                .mappingEventsToResponsesUsing((event, metaData) -> {
                    final Map<String, Object> map = (Map<String, Object>) event;
                    final String content = (String) map.get("content");
                    metaData.set(STRING_RESPONSE, content);
                })
                .configuredBy(configurator -> {
                    configurator.configureSecurity().addAuthenticator(authenticator);
                    configurator.configureSecurity().addAuthorizer(authorizer);
                    configurator.configureLogger().loggingToStderr();
                    configurator.registerModule(webSocketModule);
                    configurator.registerModule(aBodyMapParsingModule()
                            .parsingContentType(json()).with(body -> new Gson().fromJson(body, Map.class))
                            .usingTheDefaultContentType(json()));
                });

        return testConfiguration(httpMate, webSocketModule);
    }
}
