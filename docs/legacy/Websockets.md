# Websockets

HttpMate has first-level support for websockets.

## Configuration
Websocket support can be activated by adding the following configuration:
```java
[...]

.thatIs().configured(toUseWebSockets().acceptingWebSocketsToThePath("/connect").taggedBy("WEBSOCKET")

[...]
```
This will tell HttpMate to accept websockets on the `/connect` route. Websockets connected to the
`connect` route will be *tagged* with the string `WEBSOCKET`. The tag can be used to identify
websockets according to specific criteria.
Depending on the HttpMate flavour you are using, websockets will integrate differently
into your system. 

### Low-Level flavour
Consider a low-level HttpMate configuration that activates websockets according to the
previous section:
```java
aLowLevelHttpMate()
        .thatIs().configured(toUseWebSockets().acceptingWebSocketsToThePath("/connect").taggedBy("WEBSOCKET")
        .build();
```
In order to handle messages to a websocket, you need to write a handler:
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
The handler gets the websocket message as a parameter of type `String`. It returns an
`Òptional<String>`. If the `Optional` is empty, nothing will be sent in response
to the incoming message. If the `Optional` is not empty, the contained `String` will
be sent back on the websocket as a response. 

The example handler will respond to incoming websocket messages with `Hi!` if they are equal to `Hello`.
Now you need to register this handler to the HttpMate configuration:
```java
aLowLevelHttpMate()
        .callingTheHandler(new MyMessageHandler()).when(webSocketMessageIsTaggedWith("WEBSOCKET"))
        .thatIs().configured(toUseWebSockets().acceptingWebSocketsToThePath("/connect").taggedBy("WEBSOCKET")
        .build();
```
Here, the `MyMessageHandler` will be called whenever a message arrives on any websocket
tagged with `WEBSOCKET` (which is any websocket connected to the `/connect` route).

### Use-Case driven flavour
For using websockets in the use-case driven flavour, one has to activate websockets
in the HttpMate configuration:
```java
useCaseDrivenBuilder()
        [...]
        .configured(toUseWebSockets().acceptingWebSocketsToThePath("/connect").taggedBy("WEBSOCKET")
        .build();

```

In order to handle incoming websockets with usecases, you can register your
usecase like in the following example:
```java
useCaseDrivenBuilder()
        .servingTheUseCase(MyUseCase.class).on(Conditions.webSocketIsTaggedWith("WEBSOCKET"))
        [...]
        .configured(toUseWebSockets().acceptingWebSocketsToThePath("/connect").taggedBy("WEBSOCKET")
        .build();
```
Here, the `MyUseCase` will be called whenever a message arrives on any websocket
tagged with `WEBSOCKET` (which is any websocket connected to the `/connect` route).



### Event driven flavour

## Handling incoming messages

## Responding on websockets

## Sending arbitrary messages to websockets