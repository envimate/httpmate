# Routing
By now, you should have gone through the quickstart example, know how
to set up a minimal HttpMate instance and run it via the `PureJavaEndpoint`.
The configuration looked like this:
```java
final HttpMate httpMate = anHttpMate()
        .get("/hello", (request, response) -> response.setBody("hi!"))
        .build();
```
As you can see, you configured HttpMate to serve the `/hello` route.
This chapter will explain which options you have for specifying
these routes.

## Path parameters
You can specify parameterized routes in this manner:
```
/items/<itemId>
```
Here, a call to e.g. `/items/milk` would match the route and the `itemId` parameter would resolve to `milk`.
The next chapter will explain how to access the parameter in request processing.

## Regular expressions
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

## Wildcards
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