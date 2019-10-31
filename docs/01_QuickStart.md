# QuickStart

If you are new to HttpMate, start here to learn how to use it.

## Adding HttpMate to your project
Before you can start to use HttpMate, you need to add the necessary dependencies to your codebase.
This guide assumes you are using Maven, but it will work with Gradle etc. accordingly.
Add this to your pom.xml:
```xml
<dependency>
    <groupId>com.envimate.httpmate</groupId>
    <artifactId>httpmate-core</artifactId>
    <version>${httpmate.version}</version>
</dependency>
```

## Configuring HttpMate
HttpMate is very intuitive to configure. In this example, we
will configure a simple route: 
```java
final HttpMate httpMate = anHttpMate()
        .get("/hello", (request, response) -> response.setBody("hi!"))
        .build();
```
This will respond to all GET requests to the `/hello` route with the string `"hi!"`

## Adding an endpoint
HttpMate by itself is infrastructure agnostic. When you want to actually serve
requests, you have to combine it with an endpoint. There are endpoints for
different ways to serve http requests, like using a standalone Jetty instance
or integrating into a servlet engine. In our example, we will facilitate the
so-called `PureJavaEndpoint`, which is implemented on top of the native Java http
server and has therefore a minimal dependency footprint:
```java
pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
```
You can now point a web browser of your choice to http://localhost:1337/hello and
see the expected `"hi!"` message.

