# HttpMate Client

Additionally to HttpMate's main server functionality, it can also be used as a http client. In
order to do this, you need to include the client integration.

```xml
<dependency>
    <groupId>com.envimate.httpmate.integrations</groupId>
    <artifactId>httpmate-client</artifactId>
    <version>${httpmate.version}</version>
</dependency>
```
## Configuring a client
Before a client can be used, it needs to be configured. Example:

```java
final HttpMateClient httpMateClient = aHttpMateClientForTheHost("example.org")
                .withThePort(8080)
                .viaHttps()
                .build();
```

The first three configuration options deal with connecting the client to a server.
In the example, the client is configured to send all requests to port `8080` on the `example.org` host. The
protocol to use is `https` (the alternative being `http`).

There are additional methods to configure the client:

- `withBasePath()` - configures the so-called base path. A base path is a path that gets prefixed to all requests. Given for example
                     a client configured to use the base path `/rest/api` - when a request to e.g. `/contacts` gets
                     issued, the client will extend the path to `/rest/api/contacts` by prefixing with the base path.

- `withDefaultResponseMapping()` - configures how the client can map the requests' responses. When issuing requests,
the client is always explictly given a class type that the user wants the response to be mapped to. There are default implementations
for `String` and `SimpleHttpResponseObject` (which will let you access all metadata). If you intend to map the response to types other than
that, you need to provide mappings for them.

- `withResponseMapping()` - same as `withDefaultResponseMapping()`, but configures a response mapping only for a specific type.

## Using a client
A configured client can be used to issue http requests to the configured server. Examples:

```java
final SimpleHttpResponseObject response = httpMateClient.issue(aGetRequestToThePath("/foo"));
```

```java
final String response = httpMateClient.issue(aGetRequestToThePath("/foo").mappedToString());
```

```java
final OrderConfirmation orderConfirmation = httpMateClient.issue(aPostRequestToThePath("/placeOrder").withTheBody("{articleId: 4324923}").mappedTo(OrderConfirmation.class));
```

```java
httpMateClient.issue(aPostRequestToThePath("/upload").withAMultipartBodyWithTheParts(aPartWithTheControlName("file").withTheFileName("file.txt").withTheContent(myStream)))
```

## Using HttpMate client and server together
Sometimes you would use a HttpMate client to connect to a HttpMate server running in the same JVM.
This could happen e.g. in an integration test scenario. In order to speed up the execution time
of your tests, the possibility exists to connect the client directly to your server, bypassing
all "real" http handling. You can create such a client like this:

```java
final HttpMateClient httpMateClient = aHttpMateClientBypassingRequestsDirectlyTo(httpMate).build();
```