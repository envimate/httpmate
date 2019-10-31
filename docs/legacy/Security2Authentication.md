# Security II: Authentication
Authentication is the process of identifying
When securing web applications, authenticating

HttpMate performs authentication on a per-request level.

In order to authenticate requests, you need to implement the `Authenticator` interface:

```java
public final MyAuthenticator implements HttpAuthenticator {
    
    @Override
    public Optional<?> authenticateAs(final HttpRequest request) {
        final Headers headers = request.headers();
        final String userId = headers.getHeader("userId").orElseThrow();
        final String password = headers.getHeader("password").orElseThrow();
        final User user = userRepository.loadUser(userId, password);
        return Optional.ofNullable(user);
    }
}
```
An authenticator returns an `Optional`. An empty `Optional` would indicate to HttpMate that the request has not been authenticated.
If the `Òptional` has a value, the request has been authenticated and the value of the `Optional` can be accessed in further stages
under the name `AUTHENTICATION_INFORMATION`.

The `MyAuthenticator` authenticator can now be registered in an HttpMate configuration. This works the same for any flavour, but
the following example is using the low-level flavour: 

```java
aLowLevelHttpMate()
        .get("/hello", (request, response) -> response.setBody("hi!"))
        .thatIs().configured(toAuthenticateRequests().beforeBodyProcessing().using(new MyAuthenticator()))
        .build();
``` 