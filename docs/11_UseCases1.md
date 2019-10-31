# Usecases I: Basics

This chapter assumes you understand basic http and HttpMate concepts,
especially the mechanics of marshalling and unmarshalling.

So far, we showed you how to handle requests in a very low-level fashion
using the `HttpHandler` interface in the examples.
This way, you can directly access request and response features like headers and query
parameters.
In real projects, you would call domain logic from the handlers in order to do something
productively and keep an architectural boundary between infrastructure code (HttpMate)
and business / domain logic. Depending on which design "philosophy" you follow, you might
call the entry points into your domain logic services, usecases, or even something else.
In order to maintain consistency, throughout HttpMate we will call this concept a *usecase*.

Let's design a simple usecase:
```java
public final class PingUseCase {

    public void ping() {
        System.out.println("Ping!");
    }
}
```
As you can see, the `PingUseCase` is a POJO class with a single public method `ping()`.
Note how the class does not contain a single dependency on any infrastructure (read: HttpMate) code.
If we want to serve this usecase via HttpMate using the constructs we know so far,
we will probably end up with something like this:
```java
final HttpMate httpMate = anHttpMate()
        .get("/ping", (request, response) -> {
            final PingUseCase pingUseCase = new PingUseCase();
            pingUseCase.ping();
        })
        .build();
```
We registered a handler that instantiates the usecase and then calls it.
Since serious projects might contain hundreds of usecases, this way
of registering them to HttpMate seems like a lot of boilerplate code
and a violation of the *Don't repeat yourself* design principle.

# Serving usecases directly
HttpMate mitigates these problems by offering to directly serve usecases.
Instead of calling the domain logic from handlers, you register the usecase
classes directly to HttpMate.

<!--
In real projects, you would have to map these features to actual domain logic.
With increasing project size and complexity, managing requests and mapping them to domain
logic becomes unfeasable.
HttpMate catches these architectural requirements by offering to serve so-called usecases.
Instead of calling the domain logic from handlers, you register it to HttpMate
on a much higher level in the form of usecases. These are classes with one single public method
that will reflect one single feature of your application. For example:
-->

In order to do this, you need to add the `httpmate-usecases` dependency to your project:
```xml
<dependency>
    <groupId>com.envimate.httpmate.integrations</groupId>
    <artifactId>httpmate-usecases</artifactId>
    <version>${httpmate.version}</version>
</dependency>
```
HttpMate will automatically discover the dependency and support usecases.
Afterwards, you can change the configuration and shrink it
down significantly:
```java
final HttpMate httpMate = anHttpMate()
        .get("/ping", PingUseCase.class)
        .build();
```

HttpMate will now direct `POST` requests on the `/ping` route to the `ping()` usecase method.
You can try this by running your application. Once you browse to http://localhost:1337/ping, you should see
the `Ping!` message pop up on the console.
