[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.envimate.httpmate/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.envimate.httpmate/core)

<img src="httpmate_logo.png" align="left"/>

# HttpMate

HttpMate is an http framework that allows you to "just publish my business logic as HTTP endpoint".
It's non-invasive, flexible and ultra-extendable and offers you 3 modes of handling http requests - UseCase driven, 
low-level http and event-driven request handling.

Let's see some low-level example:

```
final HttpMate httpMate = HttpMate.aLowLevelHttpMate()
        .callingTheHandler(new HttpHandler() {
            @Override
            public void handle(final HttpRequest request, final HttpResponse httpResponse) {
                httpResponse.setBody("Hello World!");
                httpResponse.setStatus(200);
            }
        })
        .forRequestPath("/api/hello")
        .andRequestMethod(HttpRequestMethod.GET)
        .thatIs()
        .build();
```

Treat HttpMate instance as a description of your endpoints: we have here a request handler, for the path `api/hello`, 
with the request method `GET`, which handles the request by setting the response to the String `Hello World!` and the 
status to 200. Pretty descriptive right?

This way of saying hello gives you full control over the HTTP protocol. Once your UseCase is more complicated than just 
saying hello, you want to focus on implementing it instead of dealing with protocol details.

Let's say we have a UseCase of sending an email:

```
public class SendEmail {
    private final EmailService emailService;

    //    @Inject if you so wish
    public SendEmail(final EmailService emailService) {
        this.emailService = emailService;
    }

    public Receipt sendEmail(final Email email) {
        final String trackingId = emailService.send(email.sender, email.receiver, email.subject, email.body);
        final String timestamp = String.valueOf(Instant.now().toEpochMilli());

        return new Receipt(trackingId, timestamp);
    }
}
```

Now we can expose this UseCase using HttpMate:

```
final HttpMate useCaseDrivenHttpMate = HttpMate.anHttpMateConfiguredAs(UseCaseDrivenBuilder.USE_CASE_DRIVEN)
        .servingTheUseCase(SendEmail.class)
        .forRequestPath("/api/sendEmail")
        .andRequestMethod(HttpRequestMethod.POST)
        .mappingRequestsAndResponsesUsing(
                mapMate()
                        .mappingAllStandardContentTypes()
                        .assumingTheDefaultContentType(ContentType.json())
                        .bySerializingUsing(SERIALIZER)
                        .andDeserializingUsing(DESERIALIZER))
        .configured(Configurators.toCreateUseCaseInstancesUsing(INJECTOR::getInstance))
        .build();
```

Want to extract the sender from the authorization, the receiver and subject from path and 
the email contents from the request body?

```
final HttpMate useCaseDrivenHttpMate = HttpMate.anHttpMateConfiguredAs(UseCaseDrivenBuilder.USE_CASE_DRIVEN)
        .servingTheUseCase(SendEmail.class)
        .forRequestPath("/api/sendEmail/<receiver>/<subject>")
        .andRequestMethod(HttpRequestMethod.POST)
        .mappingRequestsAndResponsesUsing(
                mapMate()
                        .mappingAllStandardContentTypes()
                        .assumingTheDefaultContentType(ContentType.json())
                        .bySerializingUsing(SERIALIZER)
                        .andDeserializingUsing(DESERIALIZER))
        .configured(Configurators.toCreateUseCaseInstancesUsing(INJECTOR::getInstance))
        .configured(Configurator.configuratorForType(EventModule.class, eventModule -> {
            eventModule.setDefaultRequestToEventMapper(RequestToEventMapper.byDirectlyMappingAllData());
        }))
        .configured(com.envimate.httpmate.security.Configurators.toAuthenticateRequests().afterBodyProcessing().using(new Authenticator() {
            @Override
            public Optional<?> authenticateAs(final MetaData metaData) {
                final Optional<String> jwtToken = metaData.get(HttpMateChainKeys.HEADERS).getHeader("Authorization");
                final Optional<String> userEmail = TOKEN_SERVICE.decrypt(jwtToken);
                userEmail.ifPresent(email -> {
                    metaData.get(BODY_MAP).put("sender", email);
                });
                return userEmail; 
            }
        }))
        .build();
```

Want to use the very same UseCase to handle SendEmail events coming from an SNS topic? Refer to the 
"Event Driven HttpMate" (TODO) part of the README to see what modifications should be done to the httpMate object to 
achieve that. 

## What is HttpMate doing for you?

> A good architecture is less about the decisions you make and more about the decisions you defer making.

HttpMate allows you to write your UseCases decoupled from the underlying hazards of an Http/Rest infrastructure.

Debating questions like:
 
- "Should it be a PUT or a POST?"
- "Is the Username coming from the request body, the JWT token or a plain text header value?"
- "Are we talking JSON, YAML, XML or a custom (binary?) ContentType?"

is tiresome because you can't possibly know the answer until you've faced the customer. Furthermore, he might just change
his mind.   

And that's what HttMate is doing for you.

## Other Features

Besides providing you with the described interface to build http request handlers, expose UseCases or handle events, 
HttpMate offers following features:

* Several endpoint integrations such as 
        - AWS Lambda
        - Jetty
        - Spark
        - Servlet
* Integration with (de)serialization framework MapMate
* Websockets
* Predefined CORS configurations
* Predefined MultiPart support 

## Why another HTTP framework?

_The goal of refactoring is to actively counteract the natural increase in the degree of chaos_ 

We did not find any framework that would allow us to develop a web application and claim in good conscience that it's 
business logic does not depend on the underlying HTTP server, persistence layer or (de)serialization mechanism (also
referred to as "infrastructure code" in DDD).

## Getting started

 Add the dependency:

```
<dependency>
    <groupId>com.envimate.httpmate</groupId>
    <artifactId>core</artifactId>
    <version>1.0.19</version>
</dependency>
```

Configure HttpMate with an HttpHandler and expose as a PureJavaEndpoint

```
public class Application {
    public static void main(String[] args) {
        final HttpMate httpMate = HttpMate.aLowLevelHttpMate()
                .callingTheHandler(new HttpHandler() {
                    @Override
                    public void handle(final HttpRequest request, final HttpResponse httpResponse) {
                        httpResponse.setBody("Hello World!");
                        httpResponse.setStatus(200);
                    }
                })
                .forRequestPath("/api/hello")
                .andRequestMethod(HttpRequestMethod.GET)
                .thatIs()
                .build();

        PureJavaEndpoint.pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
    }
}
```

Run the example and try

```
    curl http://localhost:1337/api/hello
```

## Changing the endpoint

Since HttpMate separates the _how_ is from the _what_, you can focus on defining _what_ your http endpoints should do and decide on _how_ to serve them best separately, based on the requirements of your infrastructure.
 
To expose the same httpMate instance using a Jetty endpoint, include the following dependency:

```
<dependency>
    <groupId>com.envimate.httpmate.integrations</groupId>
    <artifactId>httpmate-jetty</artifactId>
    <version>${httpmate.version}</version>
</dependency>
```

And replace the `PureJavaEndpoint` line with:

```
    JettyEndpoint.jettyEndpointFor(httpMate).listeningOnThePort(1337);
```

Restart the application and enjoy the benefits of Jetty.

## Servlet

...

## Detailed Examples

In the following sections we will explore different features of HttpMate and show detailed examples.
You can also check out the examples submodule as well as the tests of HttpMate.

### Low level HttpMate

`HttpMate.aLowLevelHttpMate()` is a step builder to configure HttpMate when dealing with requests directly.

Step1. Configure the handler
In it's most generic form, handler must implement the `com.envimate.httpmate.handler.Handler` interface, which gives you access to a MetaData object containing all details of the request 
and means to communicate the response.

For details about the MetaData object refer to a section [What is MetaData?](#What-is-MetaData?)

Step2. Configure the request path

Step3. Configure the request method(s) this handler is answering to

Step4. Apply additional Configurators
 

#### What is MetaData?

MetaData allows access to http request details and provides means of communicating the response back.

`com.envimate.httpmate.HttpMateChainKeys` class provides a list of keys that are accessible in the MetaData object.
e.g. the `BODY_STRING` key will give you access to the body of the request represented as a String, `HEADERS` will give
you access to get the headers, etc. 

Here's an example of how it will look like with MetaData to get the name of the user from the query parameters vs the 
convenience HttpRequest object

```
final HttpMate httpMate = HttpMate.aLowLevelHttpMate()
        .callingTheHandler(new HttpHandler() {
            @Override
            public void handle(final HttpRequest request, final HttpResponse httpResponse) {
                final Optional<String> name = request.queryParameters().getQueryParameter("name");
                httpResponse.setBody("Hello " + name.orElse("World!"));
                httpResponse.setStatus(200);
            }
        })
        .forRequestPath("/api/hello")
        .andRequestMethod(HttpRequestMethod.GET)
        .callingTheHandler(new Handler() {
            @Override
            public void handle(final MetaData metaData) {
                final Optional<String> name = metaData.get(QUERY_PARAMETERS).getQueryParameter("name");
                metaData.set(RESPONSE_STRING, "Hello " + name.orElse("World!"));
                metaData.set(RESPONSE_STATUS, 200);
            }
        })
        .forRequestPath("/api/helloDirect")
        .andRequestMethod(HttpRequestMethod.GET)
        .thatIs()
        .build();
```
