# Using HttpMate with AWS Lambda

A common way to write http endpoints nowadays is the ApiGateway integration with AWS Lambda. 
HttpMate Lambda integration allows you to _delegate_ the handling of request which comes from ApiGateway to one of your UseCases.

In this example we will implement the `com.amazonaws.services.lambda.runtime.RequestHandler` and pass the `APIGatewayProxyRequestEvent`
to a simple `HelloWorld` usecase.

1. Include the right dependencies

```xml
    <dependency>
        <groupId>com.envimate.httpmate.integrations</groupId>
        <artifactId>httpmate-awslambda</artifactId>
        <version>1.0.1</version>
    </dependency>
```


Once included this will make the `AwsLambdaEndpoint` class available for you.

All of our examples follow the packaging model, where we separate the so-called "Infrastructure" code, from your business logic:

```text
├── pom.xml
├── README.md
└── src
    └── main
        └── java
            └── com
                └── envimate
                    └── httpmate
                        └── examples
                            └── awslambda
                                ├── infra
                                │   └── HttpMateIntegration.java  ---> Integration code, can change based on where/how you want to run your endpoint
                                └── usecases  ---> Actual business logic - UseCases, and related Domain Objects
                                    ├── HelloRequest.java
                                    ├── HelloResponse.java
                                    ├── HelloUseCase.java
                                    └── ValidationException.java
```


Implementation of the `com.amazonaws.services.lambda.runtime.RequestHandler` will look as follows: 

```java
public class HttpMateIntegration implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final AwsLambdaEndpoint ENDPOINT;

    static {
        final Gson gson = new Gson();
        ENDPOINT = awsLambdaEndpointFor(HttpMate
                .aSimpleHttpMateInstanceWithSecureDefaults()
                .servingTheUseCase(HelloUseCase.class)
                .forRequestPath("api/hello")
                .andRequestMethod(HttpRequestMethod.POST)
                .mappingRequestsToUseCaseParametersUsing(gson::fromJson)
                .serializingResponseObjectsUsing(gson::toJson));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent event, final Context context) {
        return ENDPOINT.delegate(event, context);
    }
}
```

As you can read from the static initializer of httpmate-enabled endpoint we will:

1. Handle the `api/hello` request path
2. answering `POST` requests
3. by invoking the `HelloUseCase` class upon request
4. and mapping the request and response objects using `Gson`

Of course this is a over simplified example to showcase the lambda integration of httpmate. 
In case this is not enough for your usecase, we encourage the usage of the dependency injection framework of your choice.

## Build and deploy

As usual for AWS lambdas, we need to build a jar and deploy it to aws. Using the shade maven plugin as suggested (by aws)[https://docs.aws.amazon.com/lambda/latest/dg/java-create-jar-pkg-maven-no-ide.html]

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>2.3</version>
                <configuration>
                    <createDependencyReducedPom>false</createDependencyReducedPom>
                    <finalName>httpmate-aws-lambda</finalName>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```


Choose a bucket where you would keep your jar and deployment templates.

Make sure the bucket exists:

```bash
aws s3 mb ${YOUR_BUCKET}
```

Build the example jar.

```bash
mvn clean verify
```

upload the jar and the packaged deployment template

```bash
aws cloudformation package \
    --template-file ./lambda-with-api-gateway.yaml \
    --s3-bucket ${YOUR_BUCKET} 
    --output-template-file target/packaged-lambda-with-api-gateway.yaml
```

Apply the cloudformation:

```bash
aws cloudformation deploy \
    --template-file target/packaged-lambda-with-api-gateway.yaml \
    --stack-name httpmate-lambda \
    --parameter-overrides StackIdentifier=nisabek-http-lambda \
    --capabilities CAPABILITY_NAMED_IAM
```

This returns the URL of the deployed lambda 

```bash
aws cloudformation describe-stacks --stack-name httpmate-lambda --query 'Stacks[0].Outputs[0].OutputValue'
```

Now we can test our endpoint by executing a simple `curl`

```bash
curl -XPOST --data '{"name":"World"}' "$(aws cloudformation describe-stacks --stack-name httpmate-lambda --query 'Stacks[0].Outputs[0].OutputValue' --output text)"/api/hello 
```
