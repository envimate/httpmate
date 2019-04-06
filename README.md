#HttpMate

_"Make use cases great again" -Source Unknown_

Following Robert Martin's [Clean Architecture](https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html) proposal, HttpMate builds an _interface adapter_ for use cases implemented on the jvm, with the following characteristics:

- use case classes need not import anything from the httpmate package (⓵)
- the configuration of httpmate is done once at application startup using a fluent builder, making autocompletion possible, and intent clearer (⓶)
- the resulting adapter becomes a web endpoint using the httpmate spark integration now, with more integrations coming later (⓷)

HttpMate publishes your business UseCases as http endpoints. You are free to chose _how_ you run the underlying server, serving the http requests yourself.

HttpMate provides integrations with

* embedded jetty
* spark
* aws-lambda

The example below works through httpmate mechanics on spark example

Show me the code
----------------
```xml
<!-- maven pom.xml -->
<dependency>
    <groupId>com.envimate.httpmate.integrations</groupId>
    <artifactId>httpmate-spark</artifactId>
    <version>1.0.6</version>
</dependency>
```
---
```java
package hello.usecases; // ⓵

public class HelloUseCase {
    public String sayHello(String who) {
        if ("throw".equals(who)) {
            throw new RuntimeException("\uD83D\uDE23");
        } else {
            return String.format("Hello %s!", who);
        }
    }
}
```
---
```java
package hello.webapp;

import com.google.gson.Gson;
import hello.usecases.HelloUseCase;
import static com.envimate.httpmate.Core.*;

public class HelloWeb {
    public static void main(String[] args) {
        aHttpMateInstance() // ⓶
            .servingTheUseCase(HelloUseCase.class).forRequestPath(httpRequestPathTemplate("/hello")).andRequestMethod(postMethod()) // ⓸
            .obtainingUseCaseInstancesUsing((usecase, webRequest) -> usecase.useCaseClass == HelloUseCase.class? new HelloUseCase() : null) // ⓹
            .mappingAllOtherExceptionsBy(exception -> theExceptionResponse(internalServerError(), exception.toString())) // ⓺
            .mappingRequestBodiesToUseCaseParametersUsing((webRequest, targetClass) -> new Gson().fromJson(webRequest.body, targetClass)) // ⓻
            .serializingResponseObjectsToResponseBodiesUsing(result -> new Gson().toJson(result)) // ⓼
            .usingForSerializedResponsesTheContentType("application/json") // ⓽
            .usingTheEndpoint(sparkEndpoint()).listeningOnPort(8000) // ⓷⓾
            .listen();
    }
}
```
---
```shell
$ curl -X POST -H "Content-Type: application/json" -d "\"envimate\"" http://localhost:8000/hello
"Hello envimate!"

$ curl -X POST -H "Content-Type: application/json" -d "\"throw\"" http://localhost:8000/hello
"java.lang.RuntimeException: 😣"
```

What is going on
----------------
- ⓸ Use case routing: maps use cases to request paths and methods
- ⓹ Decide how you want to instantiate the use cases (guice injector, spring context, etc...). The callback is invoked on every request
- ⓺ Map other (usually unexpected) runtime exceptions. Here we only return the exception toString() representation
- ⓻ Map request parameters to objects expected by the use case. These are usually in json format for REST endpoints
- ⓼ Map use case result objects and exception response result objects to serialized text form
- ⓽ Indicate what text-based mime type the response is in. This is usually "application/json" for REST endpoints
- ⓾ Hookup with a web framework to expose the use case to the web.

Integrating with Dependency Injection frameworks
------------------------------------------------

Use cases have dependencies that need to be provided at construction time, either by you (if you call _new_, and _new_-like factory methods directly), or by a dependency injection framework.
This section shows how you can make use of dependency injection frameworks in conjunction with httpmate.

Since the use case class must not be modified to import framework specific objects, the example use case class won't use framework-specific annotations such as `@Inject` or `@Autowired` on the use case itself.

###Use Case
The use case used for dependency injection integration is a variation of the previous one, with an added dependency on a translation service, which will translate the greeting to a language preferred by the client.

```
public class TranslatedHelloUseCase {
    private final TranslationService translationService;

    public TranslatedHelloUseCase(final TranslationService translationService) {
        this.translationService = translationService;
    }

    public String sayTranslatedHello(final Locale targetLocale, final String who) {
        final String translated = translationService.translate(String.format("Hello %s!", who), targetLocale);
        return translated;
    }
}
```

### Google Guice
Guice is configured to instantiate use case instances using a *Module*. Here, we need to configure the providers for the usecase and the concrete translation service.

```java
final Injector injector = Guice.createInjector(new AbstractModule() {
    @Provides
    TranslatedHelloUseCase translatedHelloUseCase(TranslationService translationService) {
        return new TranslatedHelloUseCase(translationService);
    }
    @Provides
    TranslationService translationService() {
        return new AwsTranslationService(
                DefaultAWSCredentialsProviderChain.getInstance(), Regions.EU_WEST_1);
    }
});
aHttpMateInstance()
    ...
    .obtainingUseCaseInstancesUsing((usecase, webRequest) ->
        injector.getInstance(usecase.useCaseClass)) // ⓹
```

### Spring Framework

Spring uses java `@Configuration` classes in place of Guice's _Module_...

```java
@Configuration
public static class Config {
    @Bean
    public TranslatedHelloUseCase translatedHelloUseCase(final TranslationService translationService) {
        return new TranslatedHelloUseCase(translationService);
    }

    @Bean
    public TranslationService translationService() {
        return new AwsTranslationService(
                DefaultAWSCredentialsProviderChain.getInstance(), Regions.EU_WEST_1);
    }
}
```
..and an application context must be told to scan for config annotations in a package or class:
```java
final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
ctx.register(Config.class);
ctx.refresh();
ctx.registerShutdownHook();
aHttpMateInstance()
    ...
    .obtainingUseCaseInstancesUsing((usecase, webRequest) ->
        ctx.getBean(usecase.useCaseClass)) // ⓹

```
### PicoContainer

PicoContainer uses a *DefaultPicoContainer* class which is able to instantiate components through annotationless constructor taking one or more arguments out of the box. This is something other frameworks do not allow without more complex configuration. It is therefore added here for completeness:

```java
final DefaultPicoContainer pico = new DefaultPicoContainer();
pico.addComponent(Regions.EU_WEST_1);
pico.addComponent(DefaultAWSCredentialsProviderChain.getInstance());
pico.addComponent(AwsTranslationService.class);
pico.addComponent(TranslatedHelloUseCase.class);
aHttpMateInstance()
    ...
    .obtainingUseCaseInstancesUsing((usecase, webRequest) ->
        pico.getComponent(usecase.useCaseClass)) // ⓹
```
