# Multipart uploads
The multipart standard is used to implement file uploads in http.
This chapter explains how it is handled in HttpMate.

In order to upload a file to our soon-to-be-implemented server, we need to give the user the
ability to choose the uploaded file. This is achieved by the following web frontend that we will create
in the classpath as the Java resource `upload.html`:

```html
<html>
<body>
	<h1>Simple File Uploads in Java</h1>
	<form action="/upload" method="post" enctype="multipart/form-data">
	    <input type="file" name="myFile" />
	    <input type="submit" value="Upload" />
	</form>
</body>
</html>
```
We can now serve this with HttpMate:
```java
final HttpMate httpMate = anHttpMate()
                .get("/upload", (request, response) -> response.setJavaResourceAsBody("upload.html"))
                .post("/upload", (request, response) -> System.out.println(request.bodyString()))
                .build();
```
When browsing to http://localhost:1337/upload, you will see our form. Once you uploaded a file,
the `POST` route at `/upload` is triggered and HttpMate will just dump the request to the console.
You should see something like this:

```
-----------------------------30773898518035975542002665708
Content-Disposition: form-data; name="myFile"; filename="test.txt"
Content-Type: text/plain

foo

-----------------------------30773898518035975542002665708--
```

What you see is a so-called *multipart* request. When uploading files the way we did it with the form,
the browser will split the request into multiple parts along the `-----------------------------30773898518035975542002665708`
lines in the example output. Note that in the example, there is only one part because we only uploaded one file.
You can also observe that the part contains two headers - `Content-Disposition` and `Content-Type`.

If we want to process requests like that with HttpMate, we need to add the multipart integration to our project:

```xml
        <dependency>
            <groupId>com.envimate.httpmate.integrations</groupId>
            <artifactId>httpmate-multipart</artifactId>
            <version>${httpmate.version}</version>
        </dependency>
```
With the `toExposeMultipartBodiesUsingMultipartIteratorBody()` configurator method of the `MultipartConfigurators` class, we
can instruct HttpMate to handle multipart requests specifically. Like the name of the method suggests, we will
receive the multipart content as a so-called `MultipartIteratorBody`. This is an object that allows us to process
multipart parts - one after the other. In order to access it, we need to register a `MultipartHandler` instead of
the established `HttpHandler`:
```java
final HttpMate httpMate = anHttpMate()
                .get("/upload", (request, response) -> response.setJavaResourceAsBody("upload.html"))
                .post("/upload", (MultipartHandler) (request, response) -> {
                    final MultipartPart part = request.partIterator().next();
                    final String content = part.readContentToString();
                    System.out.println(content);
                })
                .configured(toExposeMultipartBodiesUsingMultipartIteratorBody())
                .build();
```
As you can see, we can now query the `MultipartIteratorBody` from the `request` object and
ask for the first part with the `.next()` method.
This time, we print only the content of the uploaded file to the console.
If you restart the application and re-upload the file, you should be able to observe this.
