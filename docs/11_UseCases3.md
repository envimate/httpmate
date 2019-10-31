# Usecases III: Advanced topics

You now know how to configure HttpMate to directly serve usecases and
how to facilitate the MapMate integration to map request and responses
to usecase parameters and return values. This chapter will talk about
some aspects that arise in most projects and how to configure aspects that
are more advanced. 

## Mapping headers/query parameters/etc.
In the second usecases chapter we showed you how to map
a request body to usecase parameters and the usecase's return value
to the response body.
This is not the only way http allows data to be communicated between the caller on
the client side and the application on server side.
Given the multiplication example from the previous chapter,
crafting a `POST` request to access the webservice might not be the optimal choice.
Another option would be encoding
the `factor1` and `factor2` fields into the url itself as query parameters.
Depending on the circumstances, this could be a more usable approach
since it's accessible from a normal web browser.
Either way, HttpMate has you covered.

### Enriching requests with other data
Let's first look at request processing i.e. parsing the usecase parameters.
The established workflow looks like this:
```
Request Body    --->    Map<String, Object>    --->    Domain Object
```
If we now want to include other aspect of the request (headers, query parameters,
path parameters, authentication data, etc.), we can enrich the intermediate
map with exactly this data. The resulting workflow would look like
this:
```
Query Parameters, etc.  -----
                            |
                            V
Request Body    --->    Map<String, Object>    --->    Domain Object
```

To configure this enrichment in HttpMate, the class `ÈventConfigurators` offers
a ton of convenient configurator methods to choose from.
You can even provide more than one of them and they are cascaded in the order they were configured.

#### toEnrichTheIntermediateMapUsing()
Takes as parameter an implementation of `RequestMapEnricher` which consumes
the current intermediate map and the current `HttpRequest`. You
can make arbitrary changes to the intermediate map. 

#### toEnrichTheIntermediateMapWithAllHeaders()
Enriches the intermediate map with all request headers. Each header
is directly put into the map at top level with its header key as key and its
header value as value.

#### toEnrichTheIntermediateMapWithAllQueryParameters()
The same as `toEnrichTheIntermediateMapWithAllHeaders()`, but with
query parameters instead of headers.

#### toEnrichTheIntermediateMapWithAllPathParameters()
The same as `toEnrichTheIntermediateMapWithAllHeaders()`, but with
path parameters instead of headers.

#### toEnrichTheIntermediateMapWithAllRequestData()
The combination of `toEnrichTheIntermediateMapWithAllHeaders()`,
`toEnrichTheIntermediateMapWithAllQueryParameters()` and
`toEnrichTheIntermediateMapWithAllPathParameters()`.

#### toEnrichTheIntermediateMapWithTheAuthenticationInformationAs()
Takes as parameter a `String` that will be used as key.
If the request has been authenticated and there is an authentication information
object, it will be stored in the intermediate map at top level
under the provided key.



### Extracting response data
Now let's consider the opposite direction, where the returned domain object
gets mapped to the response body with the intermediate step of
creating a map:
```
Domain Object    --->    Map<String, Object>    --->    Response Body
```

Now, if we want some of the map to be e.g. set as a response header - instead of ending
up in the response body - we need to extract the respective data from the intermediate
map and set the headers accordingly. This procedure is visualized in this extended workflow: 
```
Domain Object    --->    Map<String, Object>    --->    Response Body
                                    |
                                    --------------->    Headers, etc.
```

To configure the extraction, you can again choose from a variety of
configurator methods in `ÈventConfigurators`:

#### toExtractFromTheResponseMapUsing()
Takes as parameter an implementation of `ResponseMapExtractor` which consumes
the current response map and the `HttpResponse` that is associated with the current request.
You can query and/or remove arbitrary values from the map and set them as headers, etc.

#### toExtractFromTheResponseMapTheHeader()
Takes a header key and a map key as parameters - when the map key is omitted, it will be
the same as the header key. If the response map contains a value under the provided map key,
the value will be removed from the map and added as a response header value with the
provided header key. 

## Dependency Injection
Until now, we assumed that all usecase classes have a public constructor with
zero arguments and HttpMate would call this constructor when instantiating the 
usecases. Of course, this assumption is often not feasible. Serious projects
oftentimes facilitate dependency injection frameworks and/or the usecase classes
have dependencies like database objects that need to be provided in the constructor.
It is very easy to reflect these requirements in the HttpMate configuration.
In order to configure usecase instantiation to your needs and e.g. register
the injector of your choice, the `UseCaseConfigurators.toCreateUseCaseInstancesUsing()`
configurator method exists. If for example you would like
to register a Guice injector, the configuration would look like this:

```java
anHttpMate()
        ...
        .configured(toCreateUseCaseInstancesUsing(injector::getInstance))
        .build();
```

<!--
## Object mapping without MapMate
Not all projects might want to use MapMate, which is perfectly fine with
HttpMate. In order to create usecase objects without it, you have several options.

### Default serializer and deserializer

### Specialized serializer and deserializer
-->