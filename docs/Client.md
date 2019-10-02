# HttpMate Client

Additionally to HttpMate's main server functionality, it can also be used as a Http client. In
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
final HttpMateClient client = aHttpMateClientForTheHost("example.org")
                .withThePort(8080)
                .viaHttps()
                .withoutABasePath()
                .mappingResponseObjectsToStrings();
```
The first three configuration options deal with connecting the client to a server.
In the example, the client is configured to send all requests to port `8080` on the `example.org` host. The
protocol to use is `https` (the alternative being `http`).
 
The next configuration option configures the so-called base path. A base path is a path that gets prefixed to all requests. Given for example
a client configured to use the base path `/rest/api` - when a request to e.g. `/contacts` gets
issued, the client will extend the path to `/rest/api/contacts` by prefixing with the base path.
The example configuration does not provide a base path, so the request paths will be used unmodified.

The last step in the configuration deals with the bodies of the requests' responses.
Depending on your application's individual needs, we offer different options here.

### Mapping the response bodies to strings
The obvious way to handle responses is to read their body into a String. This way the method of
choice in the example.

### Mapping the responses to response objects
If you want to deal with low-level http aspects like returned status codes and headers,
this option is the way to go. It will configure the client to keep all response meta data and
return them packaged in a `SimpleHttpResponseObject`.

### Providing a custom mapper
Additionally, you can provide a custom map in order to map response data to objects of your choosing.   

## Using a client
A configured client can be used to issue http requests to the configured server. Example:

```java
client.issue(aGetRequest()
                .toThePath(path)
                .withoutABody()
                .mappedTo(String.class));
```

## Using HttpMate client and server together
Sometime you would use a HttpMate client to connect to HttpMate server running in the same JVM.
This could happen e.g. in an integration test scenario. In order to speed up the execution time
of your tests, the possibility exists to connect the client directly to your server, bypassing
all "real" http handling.