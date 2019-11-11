# Usecases III: Validation
Let's add another usecase to the multiplication example. One obvious additional feature
would be to support division.
The accompanying usecase is fairly trivial:
```java
public final class DivisionUseCase {

    public CalculationResponse divide(final DivisionRequest divisionRequest) {
        final int result = divisionRequest.dividend / divisionRequest.divisor;
        return new CalculationResponse(result);
    }
}
```

with the corresponding `DivisionRequest`:
```java
public final class DivisionRequest {
    public final Integer dividend;
    public final Integer divisor;

    public DivisionRequest(final Integer dividend, final Integer divisor) {
        this.dividend = dividend;
        this.divisor = divisor;
    }

    public static DivisionRequest divisionRequest(final Integer dividend,
                                                  final Integer divisor) {
        return new DivisionRequest(dividend, divisor);
    }
}
```
To add this usecase to HttpMate, all you need to do is add one single line:

```java
final HttpMate httpMate = anHttpMate()
                .post("/multiply", MultiplicationUseCase.class)
                .post("/divide", DivisionUseCase.class)
                .configured(toUseMapMate(mapMate))
                .build();
```
Since the `DivisionRequest` class resides in the same package as `MultiplicationRequest`,
the MapMate configuration from the previous example will auto-detect the new class and does
not need to be changed.

You can try it out with the following curl command:
```bash
curl --request POST --header 'Content-Type: application/json' --data '{"dividend": "12", "divisor": "3"}' http://localhost:1337/divide
```
The output will be the correct result to the division of 12 by 3:
```json
{"result":"4"}
```

## Illegal input
Now, school taught every single one of us that there is one highly illegal thing you should never ever even think of doing: dividing by zero.
Let's go:
```bash
curl --request POST --header 'Content-Type: application/json' --data '{"dividend": "12", "divisor": "0"}' http://localhost:1337/divide
```
Unlike us, Java actually respects the laws of math and we should see the following exception on the console:
```
ERROR: java.lang.ArithmeticException: / by zero
```
with an accompanying stacktrace.

Obviously, our calculation application should be ready to deal with illegal input like this and handle it accordingly.
As with every unhandled exception in HttpMate, it is logged (to `STDERR` by default) and the request is answered with
an empty status code `500` (Internal Server Error) response.
This might not be particularly useful to the user, since a potential frontend needs to be able to tell
what exactly was wrong about the provided input. Otherwise, the user would not know how to correct it.

## Validating input
One way to tell the user what went wrong would be normal HttpMate exception mapping.
It has aleady been explained in previous chapters how this can be achieved.
An obvious downside to this approach is that we need to execute the actual division to trigger the `ArithmeticException`
and know that the input was wrong.
By the time we execute the divsion, our input should already have been validated.
This might not be obvious for the divison, but think of a more mature example, where the usecase
would not consist of a simple math operation, but of database queries with potentially corrupt data.
To mitigate this problem, we can validate the divisor in the `DivisionRequest`, so the exception would
be thrown before the usecase and calculation could be called:

```java
public final class DivisionRequest {
    public final Integer dividend;
    public final Integer divisor;

    public DivisionRequest(final Integer dividend, final Integer divisor) {
        this.dividend = dividend;
        this.divisor = divisor;
    }

    public static DivisionRequest divisionRequest(final Integer dividend,
                                                  final Integer divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("the divisor must not be 0");
        }
        return new DivisionRequest(dividend, divisor);
    }
}
```


Let's devide through zero again:
```bash
curl --request POST --header 'Content-Type: application/json' --data '{"dividend": "12", "divisor": "0"}' http://localhost:1337/divide
```

Now, we see an `UnrecognizedExceptionOccurredException` on the console. This is actually an exception of the MapMate project,
with our `IllegalArgumentException` down below the stacktrace as the causing exception:
```
Caused by: java.lang.IllegalArgumentException: the divisor must not be 0
```
This means that MapMate is not prepared to see this particular exception i.e. it does not recognise it. In this case, MapMate will abort and
re-throw the exception wrapped in the observed `UnrecognizedExceptionOccurredException` since that is the only safe thing to do.
However, if we tell MapMate that an `IllegalArgumentException` is to be expected and that it indicates a failed
validation, MapMate will behave differently. Let's tell MapMate about the exception:

```java
final MapMate mapMate = aMapMate(MultiplicationRequest.class.getPackageName())
                .usingJsonMarshaller(gson::toJson, gson::fromJson)
                .withExceptionIndicatingValidationError(IllegalArgumentException.class)
                .usingRecipe(builtInPrimitiveSerializedAsStringSupport())
                .build();
```

Now, when we devide by zero (again):

```bash
curl --request POST --header 'Content-Type: application/json' --data '{"dividend": "12", "divisor": "0"}' http://localhost:1337/divide
```
we actually get something meaningful out of it:
```json
{"errors":[{"path":"","message":"the divisor must not be 0"}]}
```
MapMate will gather all recognised validation exceptions and present all of them to us in a format that
is easy to process.


## Custom primitives
Let's take a deeper look at MapMate's validation output:
```json
{
   "errors":[
      {
         "path":"",
         "message":"the divisor must not be 0"
      }
   ]
}
```
Under the key `error`, we find a list of all occurred exceptions. Since only
the `DivisionRequest` object has thrown one, the list has only one single entry.
The entry has two fields. Under the key `message`, the message of the caught exception
is stored.
The key `path` is of more interest. Under it, MapMate stores the logical location where
the exception occured.
This can be used to easily highlight the input fields in a user form that have been filled in incorrectly.
Since the validation exception is thrown in the top level class `DivisionRequest`, the path is empty.
Ideally, we want it to contain the value `divisor`, since this is the field that is validated.
In order to achieve this, the `IllegalArgumentException` needs to be thrown in this exact field.
Currently, it is thrown in the `DivisionRequest` class.
In order for it to be located in the `divisor` field, we need to throw the exception in the class of this field.
Unfortunately, the divisor's data type is `Integer` and we cannot change the implementation of this class.
To solve the problem, we need to write our own `Divisor` data type that contains the validation:
```java
public final class Divisor {
    private final int value;

    private Divisor(final int value) {
        this.value = value;
    }

    public static Divisor parseDivisor(final String divisorAsString) {
        final int value = parseInt(divisorAsString);
        if (value == 0) {
            throw new IllegalArgumentException("the divisor must not be 0");
        }
        return new Divisor(value);
    }

    public int value() {
        return value;
    }

    public String stringValue() {
        return String.valueOf(value);
    }
}
```
We will call these kinds of classes *custom primitives* throughout this guide since
they act pretty much the same as primitive data types like int, double, or even String
(which is technically not a primitve data type but it is used like one).
In the world of domain-driven design (DDD) they are also called *value objects*,
but it does not really matter how you call them. 
They encapsulate all aspects of a specific type of data and make sure that its
value is valid. We can now change the `DivisionRequest` accordingly:

```java
public final class DivisionRequest {
    public final Integer dividend;
    public final Divisor divisor;

    private DivisionRequest(final Integer dividend, final Divisor divisor) {
        this.dividend = dividend;
        this.divisor = divisor;
    }

    public static DivisionRequest divisionRequest(final Integer dividend,
                                                  final Divisor divisor) {
        return new DivisionRequest(dividend, divisor);
    }
}
```

Also the `DivisionUseCase` needs a small change:
```java
public final class DivisionUseCase {

    public CalculationResponse divide(final DivisionRequest divisionRequest) {
        final int divisor = divisionRequest.divisor.value();
        final int result = divisionRequest.dividend / divisor;
        return new CalculationResponse(result);
    }
}
```

When you once again request the division by zero like this:
```bash
curl --request POST --header 'Content-Type: application/json' --data '{"dividend": "12", "divisor": "0"}' http://localhost:1337/divide
```
you will receive the following validation output:
```json
{"errors":[{"path":"divisor","message":"the divisor must not be 0"}]}
```
This time, the path correctly points to the affected field: `divisor`.
It can easily be used in a frontend to present the according form validation to
the user.
