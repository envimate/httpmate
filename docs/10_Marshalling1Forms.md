# Marshalling I: Forms
You should by now have a basic understanding of setting up a simple
HttpMate instance with request handlers and know how to connect
it to an endpoint. This chapter will show you how HttpMate
can help you with handling forms.

## Forms
One recurring challenge in web applications is the processing of forms.
To start, let's create a form as a Java resource named `form.html` somewhere
in the classpath:
```html
<html>
<form action="/submit" method="post">
    Name:<input type="text" name="name"><br>
    Profession:<input type="text" name="profession">
    <input type="submit" value="Send">
</form>
</html>
```
As you can see, the form will consist of two textfields (name and profession)
and a button to submit the form.
Let's configure a simple HttpMate instance to serve the form:
```java
final HttpMate httpMate = anHttpMate()
        .get("/form", theResource("form.html"))
        .post("/submit", (request, response) -> response.setBody(request.bodyString()))
        .build();
```
We configured two routes in the endpoint. The first one serves every `GET` request
to the `/form` route with our html resource.
The second one accepts `POST` requests to the `/submit` route, since the
`<form action="/submit" method="post">` line in the html suggests that
submitted forms will be sent there. The handler of this route only returns
the body of the request, so we can inspect it.

Once started, the form should be available at http://localhost:1337/form.
When you now submit the form with `Bob` as name and `Developer` as profession,
you should see the following message sent back:
```
name=Bob&profession=Developer
```
This type of key-value encoding is called *form encoded* (or `application/x-www-form-urlencoded`,
to be specific). 

Now, just returning the form encoded form submission to the sender is not
a very reasonable way to handle forms. In order to handle it productively,
we need a way to decode the key-value pairs that we received.
Luckily, HttpMate is able to this for us with an operation
called *unmarshalling*. 

## What is unmarshalling?
The concept of *unmarshalling* in the context of HttpMate is the process of
translating a `String` of a given format (in our case: *form encoded*) into a `Map<String, Object>`
that contains all the values encoded in the `String`.
For us and our form example, this means we will receive a `Map` with two
entries:
```
"name" = "Bob"
```
and
```
"profession" = "Developer"
```
which is exactly what we need.

## MapMate
Before we can configure HttpMate to unmarshall form encoded request
bodies, we need a way to actually parse form encoded bodies to
maps. Luckily, HttpMate has a sister project called MapMate that
is able to do exactly this and integrates seamlessly into HttpMate.
Let's add HttpMate's MapMate integration module to our project, so we
can access it:
```xml
<dependency>
    <groupId>com.envimate.httpmate.integrations</groupId>
    <artifactId>httpmate-mapmate</artifactId>
    <version>${httpmate.version}</version>
</dependency>
```
Now we can configure a simple MapMate instance for our HttpMate
instance to use:
```java
final MapMate mapMate = aMapMate()
                .usingRecipe(urlEncodedMarshaller())
                .build();
```
## Configuring unmarshalling
The next step would be to tell HttpMate about our MapMate instance.
We can easily do that using the `MapMateConfigurator.toUseMapMate()` configurator
method:

```java
final HttpMate httpMate = anHttpMate()
        .get("/form", theResource("form.html"))
        .post("/submit", (request, response) -> response.setBody(request.bodyString()))
        .configured(toUseMapMate(mapMate))
        .build();
```
This way, HttpMate will automatically detect any form encoded bodies
and unmarshall them.

## The body map
In order to access the unmarshalled form contents, the `HttpRequest` object
offers a method `bodyMap()`. Let's use this to change our handler to something
slightly more productive:
```java
final HttpMate httpMate = anHttpMate()
        .get("/form", theResource("form.html"))
        .post("/submit", (request, response) -> {
            final Map<String, Object> bodyMap = request.bodyMap();
            final String name = (String) bodyMap.get("name");
            final String profession = (String) bodyMap.get("profession");
            response.setBody("Hello " + name + " and good luck as a " + profession + "!");
        })
        .configured(toUseMapMate(mapMate))
        .build();
```
When you now submit the form again with the values `Bob` and `Developer`,
the resulting web page should look like this:
```
Hello Bob and good luck as a Developer!
```