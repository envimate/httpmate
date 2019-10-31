# Simple File Uploads in Java

This article will guide you to create a simple HTTP file upload server in Java without
the need of a heavy Spring or Servlet environment.

Requirements:
- An existing Java project
- Basic understanding of Java

## Organizing the project
Let's create our initial project structure:

```bash
mkdir -p simple-java-upload/src/main/java/com/envimate/examples/http
mkdir -p simple-java-upload/src/main/resources/com/envimate/examples/http
```

Let's start with the project's pom file in the root directory that we called here `simple-java-upload`:

```xml
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.envimate.examples</groupId>
    <artifactId>simple-java-upload</artifactId>
    <version>0.0.1</version>

    <dependencies>
        <dependency>
            <groupId>com.envimate.httpmate</groupId>
            <artifactId>core</artifactId>
            <version>1.0.21</version>
        </dependency>
    </dependencies>
</project>
```

Here we have:

- The standard groupId/artifactId/version definition for our project
- The single dependency on the HttpMate core library

This is enough to start developing our upload project in the IDE of choice. Most of those have support for Maven based Java projects.

## Frontend
In order to upload a file to our soon-to-be-implemented server, we need give the user the
ability to choose the uploaded file. This is achieved by the following web frontend that we will create
at `simple-java-upload/src/main/resources/com/envimate/examples/http/frontend.html`:

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
This web page will supply a form that can be used to choose a file to be uploaded. Once the user clicks
on *Upload*, the file will be sent to the `/upload` route on the web server.

In order to access the frontend, we need a small webserver. Create a 
`simple-java-upload/src/main/java/com/envimate/examples/http/Main.java` class and add the following content:

```java
public final class Main {
    
    public static void main(final String[] args) {
        final HttpMate httpMate = aLowLevelHttpMate()
                .get("/frontend.html", theResource("com/envimate/examples/http/frontend.html"))
                .build();
        pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
    }
}
```
Once you run the `main` method in your IDE of choice, you will be able to point your web browser
to `http://localhost:1337/frontend.html` and admire the marvelous upload form you have created.

## Backend
We want to receive files and save them in a specifc directory. In order to do this,
we will write a file repository that takes the soon-to-be-implemented upload in the form
of an `InputStream`, reads the stream until the end and stores everything in a file.
```java
public final class FileRepository {
    private static final Pattern validFileNames = Pattern.compile("[0-9a-zA-Z]*");
    
    private final Path directory;
    
    public FileRepository(final Path directory) {
        this.directory = directory;
    }
    
    public void store(final String fileName, final InputStream stream) {
        
    }
}
```
Note how the filename gets validated for security reasons - otherwise, an evil-minded
uploader could for example choose `../../../../../../root/.ssh/id_rsa` as filename and - 
with a little bit of luck - turn your precious little server into an involuntary bitcoin mining bot.

Now we need to add the `/upload` route to our webserver and let it call the `store` method of the
`FileRepository`. Change the `Main.java` file accordingly:
```java
public final class Main {
    
    public static void main(final String[] args) {
        final FileRepository fileRepository = new FileRepository("/some/directory");
        final HttpMate httpMate = aLowLevelHttpMate()
                .get("/frontend.html", theResource("com/envimate/examples/http/frontend.html"))
                .post("/upload", (MultipartHandler) (request, response) -> {
                    final MultipartIteratorBody parts = request.partIterator();
                    final MultipartPart part = parts.next();
                    final String fileName = part.getFileName();
                    final InputStream stream = part.getContent();
                    fileRepository.store(fileName, stream);
                })
                .build();
        pureJavaEndpointFor(httpMate).listeningOnThePort(1337);
    }
}
```
As you can see, the `/upload` route now has a handler of type `MultipartHandler`. This is because we instructed
the browser in the frontend with the `enctype="multipart/form-data"` statement to upload the files in the so-called
`Multipart` encoding. This is very common for file uploads and - as the name suggests - would allow to upload multiple
files at once. We can access all of the uploaded files one by one using the `MultipartIteratorBody` we get from the
request. Since we only upload one file, it is safe to assume that it will be sent first and we can therefore access it by simply
calling `next()`. The `MultipartPart` part object contains all relevant data - the file's name and its content.
This is enough to instruct our `FileRepository` to store the file.

You can now run the `main` method again and point a browser to `http://localhost:1337/frontend.html`. Once you upload an
actual file, you will be able to see the file in the directory you chose for the `FileRepository`.

## HTTP and file uploads
### Choosing the http method
In normal HTTP processing, the client sends a request to the server and receives an answer.
Every HTTP request has a so-called `method`. Normal requests, that for example just ask
for a specific web page, do not to contain a body.
These requests mostly use the `GET` method.
However, when the request needs to send additional data - e.g. when a form is submitted - the
request will have a body containing this data. In this case, the `GET` method is not sufficient
because it can't have a body. The most common method that supports this is
called `POST`. Since file uploads obviously need to send data to the server, we will use this
method throughout this tutorial.

### Putting the file in the body
Now that we have established, that we intend to receive `POST` requests for our file uploads,
we need to figure out how to put the file into the request's body. Since the body is just
normal data of arbitrary length, we could simply just put the entire file into the
body and be done with it.

This has two problems:
- we still need to encode the file's name somehow
- this solution does not follow common web standards

To solve these issues, the common way to put files into an HTTP request is to encode
the  
Unfortunately, that's not how things work in the web.
When you want to upload a file, you could just put the 

, which is often a web page.
Normally, these requests don't contain a body. data any additional
When the client wants to send data to the server, it needs to add 

## Simple HTTP server
In order to receive file uploads 