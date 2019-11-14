# CORS
[Cross-Origin Resource Sharing (CORS)](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
is a mechanism that is necessary
when you want websites from other sites being able to access your
webservice. This requirement most often emerges when a modern web frontend
is served on another host (or just port) than the backend. This chapter
explains how to configure HttpMate accordingly.

To add CORS to your HttpMate instance, the class `CorsConfigurators`
offers two configurator methods:
- `toActivateCORSWithAllowedOrigins()`
- `toActivateCORSWithoutValidatingTheOrigin()`

They both return a `CorsConfigurator` object for futher configuration
and differ only in the hosts they accept cross-origin requests from.
`toActivateCORSWithAllowedOrigins()` takes an arbitrary number of allowed
hostnames as parameters while `toActivateCORSWithoutValidatingTheOrigin()` simply
allows all hosts to perform cross-origin requests (use with caution).
Both configurations set the `Access-Control-Allow-Origin` header
accordingly - see [here](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Origin) for more information.

The returned `CorsConfigurator` object provides the following methods for further
configuration:

- `withAllowedMethods()` - configures the request methods cross-origin requests may
use (default is `GET` and `POST`).
This sets the `Access-Control-Allow-Methods` header - see [here](Access-Control-Allow-Methods) for more information.

- `withAllowedHeaders()` and `allowingAllHeaders()` - configures the non-standard
headers cross-origin requests may use (default is none).
This sets the `Access-Control-Allow-Headers` header in response to
the `Access-Control-Request-Headers` header - see [here](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Headers) for more information.

- `exposingTheHeaders()` and `exposingAllResponseHeaders()` - configures which non-standard response headers the browser will
expose to the caller of a cross-origin request (default is none).
This sets the `Access-Control-Expose-Headers` header - see [here](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Expose-Headers) for more information.


- `allowingCredentials()` and `notAllowingCredentials()` - configures whether
browsers should let the callers of cross-origin requests that contain credentials see the respective
response (default is to not let them).
This sets the `Access-Control-Allow-Credentials` header - see [here](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Credentials) for more information.


- `withTimeOutAfter()` - provides browsers with a timeout after which they need
 to invalidate the queried CORS information (not set by default which will
cause browsers to use their respective default settings).
This sets the `Access-Control-Max-Age` header - see [here](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Max-Age) for more information.

## Example
```java
final HttpMate httpMate = anHttpMate()
                .put("/api", (request, response) -> response.setBody("Version 1.0"))
                .configured(toActivateCORSWithAllowedOrigins("frontend.example.org").withAllowedMethods(PUT))
                .build();
```