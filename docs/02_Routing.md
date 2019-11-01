# Routing
By now, you should have gone through the quickstart example, know how
to set up a minimal HttpMate instance and run it via the `PureJavaEndpoint`.
The configuration looked like this:
```java
final HttpMate httpMate = anHttpMate()
        .get("/hello", (request, response) -> response.setBody("hi!"))
        .build();
```
As you experienced when you started the configuration, browser requests to http://localhost:1337/hello
caused the `(request, response) -> response.setBody("hi!")` lambda to be called.
If you have basic knowledge of http, this did probably not surprise you -
but if you are new, the reasons behind this might not be too obvious.
Let's shed some light onto which requests are mapped to which handling logic and - more importantly - why.

The mapping itself is called *request routing*.
Normal routing in HttpMate acts on two
features: the request method (the `.get(...)` part) and the request path (the `/hello` string).
Both concepts will be explained throughout this chapter.

## Request methods
Every http request has a specific method associated with it.
The most common one is the so-called `GET` method.
It is the only one we have used so far in the examples - this is why the
`.get("/hello", (request, response) -> response.setBody("hi!"))` line starts with `.get`.
Other common methods are `POST`, `PUT` and `DELETE`.
You can specify handlers for each of them in the same way as with the `.get()` method:
```java
final HttpMate httpMate = anHttpMate()
                .get("/test", (request, response) -> System.out.println("This is a GET request"))
                .post("/test", (request, response) -> System.out.println("This is a POST request"))
                .put("/test", (request, response) -> System.out.println("This is a PUT request"))
                .delete("/test", (request, response) -> System.out.println("This is a DELETE request"))
                .build();
```
The request method is the first thing that is looked at when routing a request.
Only when the request's method matches the method a handler is declared on, the path is taken into consideration.

## Request path
When the request method has been matched, the second feature that is considered is the request path.
It is the part behind the host declaration of a url.
As an example, in the url `https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol`, the path would
be `/wiki/Hypertext_Transfer_Protocol`.
When registering handlers with HttpMate, you need to provide a path template. The request's
path is compared to the path template to determine whether the route is a match.
The path templates you have seen so far are static templates in that they only match one particular path.
For example the path template
```
/img/island.png
```
will only be matched by a request path that is exactly equal to it:
```
/img/island.png
```

There are also more dynamic ways to specify path templates which are explained in the following paragraphs.  


### Path parameters
You can specify parameterized routes in this manner:
```
/items/<itemId>
```
Here, a call to e.g. `/items/milk` would match the route and the `itemId` parameter would resolve to `milk`.
You can access path parameters like this:
```java
final HttpMate httpMate = anHttpMate()
                .get("/items/<itemId>", (request, response) -> {
                    final String itemId = request.pathParameters().getPathParameter("itemId");
                    System.out.println("itemId = " + itemId);
                })
                .build();
```
This will be explained in broad detail soon.

### Regular expressions
If you wrap a single route element into horizontal lines (`|`), it's content will
be interpreted as a [Java regular expression](https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html).
For example:
```
/items/|item[1-9][0-9]*|
```
This route will match `/items/item1`, `/items/item2`, `/items/item3`, `/items/item25`, `/items/item345`, etc.
but not `/items/item01`, `/items/item` or `/items/item0`.

You can even add named capture groups to the route specification that will be available as path parameters:
```
/items/|item(?<itemNumber>[1-9][0-9]*)|
```
Here, a call to e.g. `/items/item5` would match the route and the `itemNumber` parameter would resolve to `5`.

### Wildcards
If you want to match arbitrary routes, you can add a single `*` to the route, which will
match an arbitrarily deep route. For example
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

## Order
With the possibility of wildcards and parameters, some requests might match
more than one route. Whenever this is the case, the route that was declared
first in the builder will take precedence.