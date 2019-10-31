# Security III: Authorization

In order to authorize requests, you need to implement the `Authorizer` interface:

```java
public final MyAuthorizer implements HttpAuthorizer<User> {
    
    @Override
    public boolean isAuthorized(final Optional<User> user, final HttpRequest request) {
        return user.map(User::getName)
                .map("admin"::equals)
                .orElse(false);
    }
}
```
An authorizer returns `true` if the request has been authorized and `false` if not.

The `MyAuthorizer` authorizer can now be registered in an HttpMate configuration. This works the same for any flavour. The following
example shows integrating it into the example configuration of the [Authentication](#authentication) section:

```java
aLowLevelHttpMate()
        .get("/hello", (request, response) -> response.setBody("hi!"))
        .thatIs().configured(toAuthenticateRequests().beforeBodyProcessing().using(new MyAuthenticator()))
        .build();
```
