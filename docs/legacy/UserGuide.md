# User Guide

This guide walks you through the features of HttpMate, how to configure HttpMate, and how to get the most out of it.

## Flavours
HttpMate comes in three different flavours: low-level, use-case driven and event driven.
Which one you need to use depends on the requirements of your project.
It is perfectly fine to start with the low-level flavour and can then later - as your project matures and gets more complex - 
easily migrate to a more advanced flavour. 
### Low-level
The low-level flavour offers features that you would expect from a normal Java web framework. You can
register handlers to URL routes and micro-manage incoming requests with access to details
like headers, query parameters, etc.
It is simple to configure:
```java
aLowLevelHttpMate()
        .get("/hello", (request, response) -> response.setBody("hi!"))
        .build();
```

#### Low-level handlers in the use-case driven builder
Any feature that has been described for the low-level flavour can also be used in the use-case driven builder. In order to
register a low-level handler in a use-case driven HttpMate configuration, you need to add a `configured` statement:
```java
useCaseDrivenBuilder()
        .post("/makeReservation", MakeReservationUseCase.class)
        .mappingRequestsAndResponsesUsing(mapMateIntegration(MAP_MATE).build())
        .configured(toHandleGetRequestsTo("/hello", (request, response) -> response.setBody("hi!")))
        .build();
```

### Event driven
Use-cases allow you to architecturally scale your application by giving you a way to configure http handling once and
then just adding new use-cases without having to add new infrastructure configuration.
The event-driven flavour will keep these properties, but will additionally allow you to scale technically. It allows you
to distribute HttpMate over different nodes by dispatching http requests to a MessageBus.

```java
anHttpMateConfiguredAs(EVENT_DRIVEN).attachedTo(messageBus)
                .triggeringTheEvent("MakeReservation").forRequestPath("/makeReservation").andRequestMethod(POST)
                .mappingResponsesUsing((event, metaData) -> metaData.set(BODY_STRING, event.toString()))
                .build();
```

This instance of HttpMate will forward any POST request on `makeReservation` to the provided message bus as an event of
type `MakeReservation`.




## Websockets
HttpMate has first-level support for websockets. In order to allow websockets in any flavour, you need to
configure your HttpMate instance accordingly;
```java
aLowLevelHttpMate()
        .thatIs().configured(toUseWebSockets().acceptingWebSocketsToThePath("/connect").taggedBy("WEBSOCKET")
        .build();
```
This will tell HttpMate to accept websockets on the `/connect` route. In order to handle messages to the websocket, you need
to write a handler:
```java
public final class MyMessageHandler implements WebSocketMessageHandler {
    
    @Override
    public Optional<String> handle(final String message) {
        if("hello".equals(message)) {
            return Optional.of("Hi!");
        } else {
            return Optional.empty();
        }
    }
}
```
This handler will accept websocket messages and respond to them with `Hi!` if they are equal to `Hello`.
Now you need to register this handler to HttpMate:
```java
aLowLevelHttpMate()
        .callingTheHandler(new MyMessageHandler()).when(webSocketMessageIsTaggedWith("WEBSOCKET"))
        .thatIs().configured(toUseWebSockets().acceptingWebSocketsToThePath("/connect").taggedBy("WEBSOCKET")
        .build();
```