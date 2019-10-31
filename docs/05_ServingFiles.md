# Serving files
A common aspect of writing web applications is to serve files. This chapter
walks you through the way this can be done with HttpMate.

## Serving from the filesystem
The feature set of early webservers was very often limited to
only serving static files. In order to serve a file with HttpMate, you
need to remember the `HttpResponse` object that you receive when
implementing the `HttpHandler` interface.
All you have to do is to call
the `setFileAsBody()` method on it.
If - for example - you want to serve an image file at `./files/image.png`, the HttpMate
configuration could look like this:
```java
final HttpMate httpMate = anHttpMate()
        .get("/myFile", (request, response) -> response.setFileAsBody("./files/image.png"))
        .build();
```
You should now see the image when browsing to http://localhost:1337/myFile.

## Serving Java resources
Sometimes, when writing web applications in Java, you would want to
serve a Java resource.
Java resources can be bundled into Java Archives (.jar-Files) and
therefore offer distinct advantages in regard to software delivery and deployment.
Resource loading can be tricky to do right,
so in order to make your life as easy as possible, HttpMate offers
a convenient integration for handling requests with resources.
Just provide the resource path via the `setJavaResourceAsBody()` to
the `HttpResponse`, and HttpMate will do the rest.

Assume you create a resource file called `myHtmlResource.html` in
your classpath with the following content:
```html
<html>
<h1>My Html Resource</h1>
This is some example content.
</html>
```
Now, to serve this resource file using HttpMate, we could use this configuration:
```java
final HttpMate httpMate = anHttpMate()
        .get("/myResource", (request, response) -> response.setJavaResourceAsBody("myHtmlResource.html"))
        .build();
```

Once the HttpMate instance is started as usual, you can access
http://localhost:1337/myResource with a browser and
see the html page rendered.

## Setting the filename of downloads
Sometimes you want to provide a file explicitly as a download, i.e. you want the browser to
prompt the user to store the served file somewhere on the local file system.
In those cases, usability benefits from providing a name suggestion for the file to be stored. 
Imagine a PDF that the client browser should present to the user as a download. In this case, depending
on the context, the name `report.pdf` might be a sensible choice.
In order to achieve this in http, the intended name suggestion needs to be encoded into to value of the `Content-Disposition` header
of the response.
The `HttpResponse` object of the `HttpHandler` interface
conveniently offers a `asDownloadWithFilename()` method to perform this task.
```java
final HttpMate httpMate = anHttpMate()
        .get("/myDownload", (request, response) -> {
            response.setBody("Hello World");
            response.asDownloadWithFilename("hello-world.txt");
        })
        .build();
```
When you browse to http://localhost:1337/myDownload, instead of seeing the `Hello World` message displayed, a dialog
should have popped up offering to store the file under the name `hello-world.txt`.

## Caching and advanced features
HttpMate was intended to facilitate architecturally sane web applications, first and foremost APIs.
The implementation of static file handling has never been a priority. Therefore, it might
currently lack features like support for caching. Nonetheless, if you need this feature or other features to exist,
please fell free to open a feature request.