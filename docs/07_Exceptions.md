# Exceptions
When something is wrong in a Java program, an exception is thrown.
This chapter explains how HttpMate will react when this occurs and how
you can map exceptions to responses.

Let's start by throwing an exception in a handler and see what happens:
```java
final HttpMate httpMate = anHttpMate()
                .get("/exception", (request, response) -> {
                    throw new RuntimeException("this is an example");
                })
                .build();
```
When running the example and navigating to `http://localhost:1337/exception`,
you should just see an empty page (with the status code `500`, but your browser
will probably not show you this).
In the console output of the application, you will see the complete stacktrace
of the thrown exception.

As you experienced, HttpMate's default behaviour for exception handling
is to log the exception and return a generic status code `500` response
with an empty body.
If you want to change this, you can use the `toMapExceptionsByDefaultUsing()`
configurator method in the `ExceptionConfigurators` class.
It is called with a lambda that processes the thrown exception and the already
explained `HttpResponse` object:

```java
final HttpMate httpMate = anHttpMate()
                .get("/exception", (request, response) -> {
                    throw new RuntimeException("this is an example");
                })
                .configured(toMapExceptionsByDefaultUsing((exception, response) -> response.setBody("Something went wrong")))
                .build();
```
If you now navigate to `http://localhost:1337/exception`, you should see
the message `Something went wrong`.

In addition to specifying the default behaviour, you can define custom responses
for specific types of exceptions.
To do this, you would use the `toMapExceptionsOfType()` configurator method
(again in `ExceptionConfigurators`).
So let's change the type of the exception to something more specific and
handle that type explicitly:
```java
final HttpMate httpMate = anHttpMate()
                .get("/exception", (request, response) -> {
                    throw new UnsupportedOperationException("this is an example");
                })
                .configured(toMapExceptionsByDefaultUsing((exception, response) -> response.setBody("Something went wrong")))
                .configured(toMapExceptionsOfType(UnsupportedContentTypeException.class, (exception, response) -> response.setBody("Operation not supported")))
                .build();
```
When starting the application and navigating to `http://localhost:1337/exception`, you should now see
the `Operation not supported` message instead of `Something went wrong`.
