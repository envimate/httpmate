# Cookies
Sometimes, websites want clients to store data locally in the browser,
e.g. encrypted credentials. 
This can be achieved with the use of cookies. Servers can instruct
browsers to store these key-value pairs using the `Set-Cookie` response header.
The browser will then send these values with every subsequent request
via the `Cookie` request header. This chapter will explain how cookies
can be handled in HttpMate.

## Setting cookies
If you just want to set a simple cookie with a key and value, you can do so using the `setCookie()` method of the `HttpResponse` object
like this:
```java
final HttpMate httpMate = anHttpMate()
                .get("/set", (request, response) -> response.setCookie("myCookie", "foo"))
                .build();
```

Cookies can have properties. If you like to set them according to yours needs, call
the `setCookie()` method with `CookieBuilder.cookie()` as parameter. This returns
an object of type `CookieBuilder` which can be used to fully customize your cookie.
It offers the following methods:

- `withExpiration()` - instructs the browser to delete the cookie after the provided expiration date by setting the `Expires` cookie directive.
Is not set by default, which will cause browsers to delete the cookie after the session ends.

- `withMaxAge()` - instructs the browser to delete the cookie after the provided time has passed by setting the `Max-Age` cookie directive.
Is not set by default, which will cause browsers to delete the cookie after the session ends.

- `exposedToAllSubdomainsOf()` - tells the browser to send the cookie with all requests to one of the specified domains (and their subdomains) by setting the
`Domain` cookie directive.
Is not set by default, which will cause the browser to send the cookie with requests to the domain from which the cookie has been set, but not subdomains of it. 
Note that most browsers will reject the cookie if the server from which the cookie is set is not included in the list.

- `exposedOnlyToSubpathsOf()` - tells the browser to only send the cookie with requests to the provided (sub-)routes by setting the `Path` cookie directive.
Is not set by default, which will cause the browser to send the cookie with every request, regardless of the route. 

- `thatIsOnlySentViaHttps()` - instructs the browser to send the cookie only with requests that are https-secured by setting the `Secure` cookie directive.
Is not set by default.

- `thatIsNotAccessibleFromJavaScript()` - instructs the browser to not send the cookie with JavaScript/AJAX requests by setting the `HttpOnly` cookie directive.
Is not set by default.

- `withSameSitePolicy()` - tells the browsers whether the cookie can be sent with cross-origin requests by setting the `SameSite` cookie directive. Possible options are
   - `STRICT` - cross-origin requests will not include the cookie
   - `LAX` - only send the cookie on certain types of cross-origin requests (see [here](https://web.dev/samesite-cookies-explained) for more information)
   - `NONE` - send the cookie with every request

    Not set by default, which will cause most browsers to default to the `NONE` policy.
    
The following example will set the `myCookie` cookie with a lifetime of two hours:
```java
final HttpMate httpMate = anHttpMate()
                .get("/setWithOptions", (request, response) -> response.setCookie(cookie("myCookie", "foo").withMaxAge(2, HOURS)))
                .build();
```

## Receiving cookies
To access the cookies that are sent with a request, the `HttpRequest` object offers
the `cookies()` method:
```java
final HttpMate httpMate = anHttpMate()
                .get("/get", (request, response) -> {
                    final String myCookie = request.cookies().getCookie("myCookie");
                    response.setBody("Value was: " + myCookie);
                })
                .build();
```

## Invalidating cookies
If you want to instruct the browser to delete a cookie, you can do so by setting the cookie again
with an expiration date that has already passed and an empty value.
HttpMate offers the `invalidateCookie` method in the `HttpResponse` object to do exactly this.
```java
final HttpMate httpMate = anHttpMate()
                .get("/invalidate", (request, response) -> response.invalidateCookie("myCookie"))
                .build();
```