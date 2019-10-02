# (Un)marshalling
Http webservices can be implemented to act on data configured in various
formats, common formats being JSON, XML or even YAML. As an application
developer, the actual format might not matter to the implementation of the
underlying business logic. One might prefer to handle the encoding and decoding
into and from these formats transparently. In order to support this,
HttpMate can perform so-called *unmarshalling* and *marshalling* operations.

## What is (un)marshalling?
The concept of *marshalling* in the context of HttpMate is the process of
translating a `Map<String, Object` into a `String` of a given format like JSON, XML or YAML.
Vice versa, *unmarshalling* is the process of translating a `String` in the given format to a
`Map<String, Object`.
As this might sound a like bit academic, the following example will clarify things.


Given a simple http request, depending on the format used in the webservice,
the body might be encoded as Json: 
```json
{
  "surname": "Joe",
  "name": "Doe"  
}
```
or as XML:
```xml
<Request>
    <surname>Joe</surname>
    <name>Doe</name>
</Request>
```
or as YAML:
```yaml
    surname: Joe
    name: Doe
```
In order to handle the incoming request transparently, all these format would be translated
into the same `Map<String, Object` with this content: 
```java
"surname" = "Joe"
"name" = "Doe"
```
This way, the request handler does not need to bother about parsing.
In the response, the request just needs to provide a `Map<String, Object` with the
content it likes to respond with, and HttpMate will manage the translation into
the correct format.
The handler might f.e. answer with a `Map<String, Object` with this content:
```java
"orderId" = "qwefgfd-gt-yeetgtr"
"status" = "SHIPPING"
```
Depending on the format to be used in the response, HttpMate will convert the map
either to Json:
```json
{
  "orderId": "qwefgfd-gt-yeetgtr",
  "status": "SHIPPING"  
}
```
or to XML:
```xml
<Response>
    <orderId>qwefgfd-gt-yeetgtr</orderId>
    <status>SHIPPING</status>
</Response>
```
or to YAML:
```yaml
    orderId: qwefgfd-gt-yeetgtr
    status: SHIPPING
```

## Configuring (un)marshalling
In order to configure HttpMate (un)marshall requests and responses, the marshalling module needs to be configured. Example:
```java
aLowLevelHttpMate()
                [...]
                .thatIs().configured(toMarshallBodiesBy()
                        .unmarshallingContentTypeInRequests(fromString("application/json")).with(body -> GSON.fromJson(body, Map.class))
                        .marshallingContentTypeInResponses(fromString("application/json")).with(map -> GSON.toJson(map))
                        .usingTheDefaultContentType(fromString("application/json")))
```
In the example, HttpMate is configured to (un)marshall Json using the Gson library. The last line configures the default `Content-Type`, which
is the `Content-Type` that is assumed if a request does not provide it's own `Content-Type` header.

## (Un)marshalling in practice
The unmarshalled bodies will be available to any request handlers. The following example will highlight how to use them:
```java
public final class MyHandler implements HttpHandler {
    @Override
    public void handle(final HttpRequest request, final HttpResponse response) {
        final Map<String, Object> requestMap = request.bodyAsMap().orElseThrow();
        final Map<String, Object> responseMap = Map.of(
                "orderId", "qwefgfd-gt-yeetgtr",
                "status", "SHIPPING");
        response.setBody(responseMap);
    }
}
```
The unmarshalled request body can be queried with the `bodyAsMap()` method of the request object.
If you call the `setBody()` method of the response object with a map as parameter, HttpMate
will continue to marshall the provided map according to its configuration.
## Multiple formats
Some applications might want to facilitate multiple formats for (un)marshalling depending
on circumstances like user customization. In order to achieve this with the marshalling integration,
it is only necessary to configure all supported formats. HttpMate will then properly
unmarshall all incoming requests according to their `Content-Type`. Marshalling
the responses is a little bit more tricky, since HttpMate needs to have a 
way to know the format it has to marshall to. There are two ways of solving this problem:
either by explicitly setting the response `Content-Type` or by calculating the response
`Content-Type` in a smart way according to criteria like the request `Content-Type` and
possibly an `Accept` header in the request. Both ways are explained in the following
two paragraphs.

### Explicitly setting the response `Content-Type`
The `Content-Type` to be used in the response can be explicitly configured using
the `setContentType()` method of the response object:
```java
public final class MyHandler implements HttpHandler {
    @Override
    public void handle(final HttpRequest request, final HttpResponse response) {
        final Map<String, Object> responseMap = Map.of(
                "orderId", "qwefgfd-gt-yeetgtr",
                "status", "SHIPPING");
        response.setBody(responseMap);
        response.setContentType("application/yaml");
    }
}
```

### Automatically determining the response `Content-Type`
HttpMate offers the option to set the response `Content-Type` i.e. marshall responses
dynamically, so that users can choose their preferred format. The dynamic handling
does not need to be explicitly configured and will be active on any response
whose `Content-Type` header has not explicitly been set.

The first thing HttpMate looks at when dynamically calculating the response `Content-Type`
is any `Accept` header in the request. HttpMate will only further consider any
`Content-Type`s that have been mentioned in the `Accept` header. If no `Accept` header
has been found, HttpMate will assume an `Àccept` header that accepts all `Content-Type`s.

In the next step, HttpMate will look at all `Content-Type`s that it actually knows how 
to marshall, i.e. for which a marshaller has been configured. It will only further
consider `Content-Type`s that are accepted by the `Accept` header and it knows how
to marshall.

From this set of accepted and supported `Content-Types`, it will pick a `Content-Type`.
If the `Content-Type` of the request is in this set, this one will be picked in
order to maintain consistency between the request's and the respons's `Content-Type`.
Otherwise, it will pick any `Content-Type` of the set. If the set is empty,
it will pick the default `Content-Type` according to the HttpMate configuration. 

## Integrating MapMate
Instead of directly using HttpMate's marshalling module, a convenient and recommended
choice would be to extract marshalling and unmarshalling to HttpMate's sister project
MapMate. In order to integrate MapMate into HttpMate, an integration module is offered
which can be configured like this:
```java
aLowLevelHttpMate()
                [...]
                .thatIs().configured(toMarshalRequestAndResponseBodiesUsingMapMate(mapMate))
                .build();
```
All settings will be automatically adapted from the provided MapMate object.