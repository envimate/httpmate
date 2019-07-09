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
### Use-case driven
The use-case driven flavour exists to catch architectural requirements of larger projects
where managing requests and mapping them to actual domain logic becomes unfeasable. 
Instead of calling the domain logic from handlers, you register it to HttpMate
on a much higher level in the form of so-called use cases. These are classes with one single public method
that will reflect one single feature of your application. For example:
```java
public final class MakeReservationUseCase {
    private final RestaurantTimetable restaurantTimetable;
    
    ...
    
    public ReservationConfirmation makeReservation(final Reservation reservation) {
        return restaurantTimetable.reserve(reservation);
    }
}
```
This is an infrastructure-agnostic way to access the domain logic.
If you decide to serve this using http, just spin a up an HttpMate with the use case flavour:
```java
useCaseDrivenBuilder()
        .post("/makeReservation", MakeReservationUseCase.class)
        ...
        .build();
```
HttpMate will now direct POST requests on `/makeReservation` to the `makeReservation` method.

#### Object mapping
Until this point, it is unclear how HttpMate manages to get the `Reservation` parameter needed to call
`makeReservation`.  In order to achieve this, you need to give HttpMate a way to map
the body of incoming requests to method parameters. The recommended way to do this is using [MapMate](https://github.com/envimate/mapmate).
Once you have configured a MapMate instance to deserialize objects of type `Reservation`, the complete HttpMate configuration will look like this:
```java
useCaseDrivenBuilder()
        .post("/makeReservation", MakeReservationUseCase.class)
        .mappingRequestsAndResponsesUsing(mapMateIntegration(MAP_MATE).build())
        .build();
```
On every request, HttpMate will now interpret the request's body according to its
`Content-Type` (e.g. Json, XML, YAML) and use the provided MapMate instance to deserialize
the necessary `Reservation` object from it. After the use case has been invoked, it will
also use the provided MapMate instance to serialize the returned `ReservationConfirmation` to a
response body of the respective format (e.g. Json, XML, YAML).

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

## Exception handling
Whenever an exceptions is thrown during the processing of a request, the processing of the request gets redirected to
an error-handling logic that will create an appropriate response. By default, every exception gets logged and the request is answered
with an empty body and status code `500`.
In order to change this behaviour, you need to implement your own exception mapper:
```java
public final class MyExceptionMapper implements HttpExceptionMapper<GreetingNotFoundException> {
    
    public void map(final GreetingNotFoundException exception, final HttpResponse response) {
        response.setStatus(501);
    }
} 
```
This mapper needs to be registered in the configuration:
```java
aLowLevelHttpMate()
        .get("/hello", (request, response) -> response.setBody("hi!"))
        .configured(toMapExceptions().ofType(GreetingNotFoundException.class).toResponsesUsing(new MyExceptionMapper()))
        .build();
```
Here, any exception of type `GreetingNotFoundException` will be mapped to the status code `501`.

## Routing
When you register handlers or use-cases to specific http routes, they get interpreted in the order they were specified when
configuring the HttpMate instance.
### Path parameters
You can specify parameterized routes in this manner:
```
/items/<itemId>
```
Here, a call to e.g. `/items/milk` would match the route and the `itemId` parameter would resolve to `milk`.

### Wildcards
If you want to match arbitrary routes, you can add a single `*` to the route, which will
match an arbitrary deep route. For example
```
/*
```
will match any request.
```
/resources/*
```
will match requests to any route that starts with `/resources`.
```
/files/*/item.xml
```
will match all requests to routes that start with `/files` and end with `/item.xml`. 

## Security
HttpMate provides easy to use and flexible ways to handle common security requirements.

### Authentication
In order to authenticate requests, you need to implement the `Authenticator` interface:

```java
public final MyAuthenticator implements HttpAuthenticator {
    
    @Override
    public Optional<?> authenticateAs(final HttpRequest request) {
        final Headers headers = request.headers();
        final String userId = headers.getHeader("userId").orElseThrow();
        final String password = headers.getHeader("password").orElseThrow();
        final User user = userRepository.loadUser(userId, password);
        return Optional.ofNullable(user);
    }
}
```
An authenticator returns an `Optional`. An empty `Optional` would indicate to HttpMate that the request has not been authenticated.
If the `Òptional` has a value, the request has been authenticated and the value of the `Optional` can be accessed in further stages
under the name `AUTHENTICATION_INFORMATION`.

The `MyAuthenticator` authenticator can now be registered in an HttpMate configuration. This works the same for any flavour, but
the following example is using the low-level flavour: 

```java
aLowLevelHttpMate()
        .get("/hello", (request, response) -> response.setBody("hi!"))
        .thatIs().configured(toAuthenticateRequests().beforeBodyProcessing().using(new MyAuthenticator()))
        .build();
```
### Authorization
In order to authorize requests, you need to implement the `Authorizer` interface:

```java
public final MyAuthorizer implements HttpAuthorizer<User> {
    
    @Override
    public boolean isAuthorized(final Optional<User> user, final HttpRequest request) {
        return user.map(User::getName)
                .map("admin"::equals)
                .orElse(false);
    }
}
```
An authorizer returns `true` if the request has been authorized and `false` if not.

The `MyAuthorizer` authorizer can now be registered in an HttpMate configuration. This works the same for any flavour. The following
example shows integrating it into the example configuration of the [Authentication](#authentication) section:

```java
aLowLevelHttpMate()
        .get("/hello", (request, response) -> response.setBody("hi!"))
        .thatIs().configured(toAuthenticateRequests().beforeBodyProcessing().using(new MyAuthenticator()))
        .build();
```

### CORS
HttpMate supports Cross-Origin Resource Sharing (CORS). You can easily configure it in any flavour:
```java
aLowLevelHttpMate()
        .get("/hello", (request, response) -> response.setBody("hi!"))
        .configured(toProtectAjaxRequestsAgainstCsrfAttacksByTellingTheBrowserThatRequests()
                .allowingTheHttpMethods(GET, POST)
                .canOriginateFromAnyHost()
                .andCanContainAnyHeader()
                .exposingNoHeadersExceptForSimpleHeaders()
                .notRequiringRessourceUsersToForwardCredentials()
                .withTheBrowserDefaultTimeOutSettings())
        .build();
```

## Downloads
HttpMate provides a simple method to serve file downloads. To use it, you need to implement
the `DownloadHandler` interface:
```java
public final class MyDownloadHandler implements DownloadHandler {
    
    public Download handle(final HttpRequest httpRequest) {
        final InputStream menuFile = loadFileAsStream("../static/menu_2019.pdf");
        return download(menuFile, "menu.pdf", "application/pdf");
    }
}
```
This handler can now be added to a HttpMate configuration:
```java
aLowLevelHttpMate()
        .get("/downloadMenu", new MyDownloadHandler())
        .build();
```
This will serve the menu as a PDF with the filename `menu.pdf`.

## Multipart uploads
HttpMate supports http multipart bodies. In order to handle them, you need to activate
multipart processing in the configuration:
```java
aLowLevelHttpMate()
        .thatIs().configured(toExposeMultipartBodiesUsingMultipartIteratorBody())
        .build();
```
Now you can define a handler for multipart requests by implementing the `MultipartHandler` interface:
```java
public final class MyMultipartHandler implements MultipartHandler {
    
    @Override
    public void handle(final MultipartRequest request, final HttpResponse response) {
        final MultipartIteratorBody iterator = request.partIterator();
        while(iterator.hasNext()) {
            final MultipartPart part = iterator.next();
            part.getFileName().ifPresent(fileName -> {
                final InputStream stream = part.getContent();
                fileRepository.store(fileName, stream);
            });
        }
    }
}
```
When receiving a multipart request, HttpMate provides you with a `MultipartIteratorBody` that can be used to conveniently
iterate over the stream of uploaded files with zero overhead. The example just accepts all files an stores them to a repository.

Once you have written the handler, you can add it to the HttpMate configuration:
```java
aLowLevelHttpMate()
        .post("/hello", new MyMultipartHandler())
        .thatIs().configured(toExposeMultipartBodiesUsingMultipartIteratorBody())
        .build();
```

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

## Endpoints
In order to actually serve a configured HttpMate instance, you have to start an endpoint. The endpoint you choose
depends on your specific deployment requirements.
### Pure Java
The most lightweight endpoint is called `PureJavaEndpoint`. It relies entirely on native Java and does not need any dependencies
other than HttpMate core.
```java
pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
```
It does not support websockets.
### Jetty
Another option is the Jetty endpoint:
```java
jettyEndpointFor(httpMate).listeningOnThePort(1337);
```
If you intend to use websockets, you can go with the Jetty-for-websockets variant:
```java
jettyEndpointWithWebSocketsSupportFor(httpMate).listeningOnThePort(1337);
```
### Spark
The Spark endpoint is another endpoint you can choose.
```java
sparkEndpointFor(httpMate).listeningOnThePort(1337)
```
It does not come with websockets support.
### Servlet
If you intend to host your application using standard Java servlet technology, you can go with the servlet endpoint.
Using it depends on how your servlet is loaded.
If you want to provide the servlet instance programmatically to your servlet engine, just create a new servlet like this:
```java
final HttpServlet servlet = servletEndpointFor(httpMate);
```

If instead you need to provide your servlet engine with a class that it can construct by itself,
you can extend the `ServletEndpoint` and provide the HttpMate instance via the super constructor:
```java
public final class MyServlet extends ServletEndpoint {
    
    private static final HttpMate httpMate = aLowLevelHttpMate() ...
    
    public MyServlet() {
        super(httpMate);
    }
}
```

There exists a version of it supporting websockets. It can be used in the same manner as the normal version:
```java
final HttpServlet servlet = webSocketAwareHttpMateServlet(httpMate);
```
or
```java
public final class MyServlet extends WebSocketAwareHttpMateServlet {
    
    private static final HttpMate httpMate = aLowLevelHttpMate() ...
    
    public MyServlet() {
        super(httpMate);
    }
}
```