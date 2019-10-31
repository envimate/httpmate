# Handling Requests
So far, you know how to configure a minimal HttpMate instance, run it with
an endpoint and understand routing.
In this chapter, we will take a closer look at basic request handlers
and how to access fine-grained http features like header values, etc.

## The `HttpHandler` interface 
Let's get back to the quickstart example configuration:
```java
final HttpMate httpMate = anHttpMate()
        .get("/hello", (request, response) -> response.setBody("hi!"))
        .build();
```
We can take the `(request, response) -> response.setBody("hi!")` lambda
and transform it into an actual class:
```java
public final class HelloHandler implements HttpHandler {

    @Override
    public void handle(final HttpRequest request, final HttpResponse response) {
        response.setBody("hi!");
    }
}
```
As you can see, the lambda implements the `HttpHandler` interface,
which provides the `handle()` method. In the `handle()` method,
you get two objects as parameters: an `HttpRequest` and an `HttpResponse`.
To no surprise, the `HttpRequest` object contains all data that
was received in the request, and the `HttpResponse` object respectively holds
all data that will be sent back as a response. Let's take a deeper look at
both of them.

## The `HttpRequest` object
`HttpRequest` holds all data that was sent in the request. The following
paragraphs will walk you through each possible aspect and explain how to
access it.

### Request Method
Every http request has a specific method associated with it.
The most common one is the so-called `GET` method which is the
only one we have used so far in the examples (this is why the
`.get("/hello", (request, response) -> response.setBody("hi!"))` line
in the quickstart example starts with `.get`).
Other common methods are `POST`, `PUT`, `DELETE` and `OPTIONS`. If you
want to explicitly query the method the current request uses,
you can do that with the `method()` method of the `HttpRequest` object. 

### Request Route and Path Parameters
Every http request has a specific route. You can query that route
of the current request using the `path()` method:
```java
public final class HelloHandler implements HttpHandler {

    @Override
    public void handle(final HttpRequest request, final HttpResponse response) {
        final Path path = request.path();
    }
}
```
If you have specified any path parameters in the HttpMate configuration,
you can access the values of these parameters via the `pathParameters()`
method. Assume this example configuration:
```java
final HttpMate httpMate = anHttpMate()
        .get("/hello/<name>", (request, response) -> {
            final PathParameters pathParameters = request.pathParameters();
            final String name = pathParameters.getPathParameter("name");
            response.setBody("hi " + name + "!");
        })
        .build();
```
When deployed the same way as the other examples using the `PureJavaEndpoint`, a request to http://localhost:1337/hello/bob
would yield the string `hi bob!`

### Query Parameters
In the http proctocol, you can append parameters to the route of a request.
When you see a request like `http://example.org/?mode=fullscreen`, everything
after the question mark are query parameters (in this case one single query
parameter with the name `mode` and the value `fullscreen`).

Query parameters can be easily accessed from the `HttpRequest` object using
the `request.queryParameters()` method. If you start this example configuration:
```java
final HttpMate httpMate = anHttpMate()
        .get("/hello", (request, response) -> {
            final QueryParameters queryParameters = request.queryParameters();
            queryParameters.getQueryParameter("name");
            response.setBody("hi " + name + "!");
        })
        .build();
```
and access http://localhost:1337/hello/?name=bob, you should again see the
message `hi bob!`.

### Request Headers
Every http request contains a set of headers, which are key-value pairs
of strings. You can access the headers of a request using the
`headers()` method.

### Request Body
Some requests carry bodies, i.e. an additional chunk of data.
You can conveniently access this body as a `String` using the `bodyString()`
method. Note that if you want to receive a body, you cannot use the `GET` request
method - recommended and common is the use of `POST` or - less common - `PUT`.

 
## The `HttpResponse` object
You can specify the response details using the `HttpResponse` object. The
following paragraphs will walk you through all available options.

### Status Code
Every http response contains a so-called status code. These codes
are standardized and tell the client whether the request could be
handled successfully and what went wrong in case of an error.
To a successful request will be responded with the status code 200 ("OK").
If something went wrong, most commonly status codes in the range
between 400-500 are used depending on the error condition, including
the famous 404 ("Not Found") code.
HttpMate will set the status code by default to 200 ("OK"). If
you want to set it manually, you can do this using the `setStatus()`
method.

### Response Headers
The same way as a request, a response can have headers. You can set
individual headers using the `addHeader()` method.
Since the `Content-Type` header is very prominent and commonly used,
there also exists the `setContentType()` method to explicitly set
this header.

### Response Body
Every http response can carry a body. You already saw how to set the
response body in the quickstart example - using the `setBody()` method.
You can provide a `String` to this method and HttpMate will send the
body accordingly. You can also give this method an `InputStream` as
parameter, and HttpMate will read the complete stream and send
it as the body. This way, you can send large bodies without the need
to allocate a huge `String` object.