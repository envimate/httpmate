# Usecases II: Object Mapping

In the first part of the usecases chapter, we looked at the ping usecase which
is actually not very useful.
It does not receive any parameters and does not return anything.
In this chapter, we will create a more realistic scenario in a webservice
that simulates a reservation to a restaurant.

## Parameters and Return Values
Let's start with defining the use case:
```java
public final class MultiplicationUseCase {

    public CalculationResponse multiply(final MultiplicationRequest multiplicationRequest) {
        final Number firstFactor = multiplicationRequest.getFactor1();
        final Number secondFactor = multiplicationRequest.getFactor2();
        final int result = firstFactor.value() * secondFactor.value();
        return new CalculationResponse(new Number(result));
    }
}
```
This usecase takes an object of type `MultiplicationRequest` as parameter:

```java
public final class MultiplicationRequest {
    public final Number factor1;
    public final Number factor2;

    public MultiplicationRequest(final Number factor1, final Number factor2) {
        this.factor1 = factor1;
        this.factor2 = factor2;
    }

    public static MultiplicationRequest multiplicationRequest(final Number factor1,
                                                              final Number factor2) {
        return new MultiplicationRequest(factor1, factor2);
    }

    public Number getFactor1() {
        return factor1;
    }

    public Number getFactor2() {
        return factor2;
    }
}
```
As you can see, a `MultiplicationRequest` simply encapsulates two factors, each of with
having the datatype `Number`:

```java
public final class Number {
    private final int number;

    public Number(final int number) {
        this.number = number;
    }

    public static Number parseNumber(final String numberAsString) {
        final int number = parseInt(numberAsString);
        return new Number(number);
    }

    public int value() {
        return number;
    }

    public String stringValue() {
        return String.valueOf(number);
    }
}
```

The `MultiplicationUseCase` will then take both factors, multiply them, and return the result
encapsulated in an object of type `CalculationResponse`:

```java
public final class CalculationResponse {
    public final Number result;

    public CalculationResponse(final Number result) {
        this.result = result;
    }

    public static CalculationResponse calculationResult(final Number result) {
        return new CalculationResponse(result);
    }
}
```

The `MultiplicationUseCase` is structured in the same way as the `PingUseCase` (a public constructor and one public method)
and again does not contain a single dependency on any infrastructure code.

We can now add the usecase to our configuration:
```java
anHttpMate()
        .post("/multiply", MultiplicationUseCase.class)
        .build();
```

If we would start the application now, a `POST` request to `/multiply` would fail, because HttpMate
does not yet know how to create the `MultiplicationRequest` parameter and what to do with the `CalculationResponse`
return value.

## Object mapping
Until this point, it is unclear how HttpMate could get the `MultiplicationRequest` parameter needed to call
`multiply()`.
One possible and common way to achieve this is to put the input data into the request body
and expect the result to be sent back in the response body.
If we encode the data using Json, an example request body could probably look like this:
```json
{
  "factor1": "4",
  "factor2": "5"
}
```
and the corresponding response body would look like this:
```json
{
  "result": "20"
}
```

Now we need a way to reflect this in HttpMate. As we know from the marshalling chapters, HttpMate integrates well with its
sister project MapMate.
We also know that MapMate is able to (un-)marshall requests and responses i.e. can convert a `String` to a `Map<String, Object>`
and vice versa.
What we didn't tell you until now is that
it can go even one step further and do conversions from a `Map<String, Object>` to domain objects and vice versa.
This additional step is called deserialization and serialization. 
Together, MapMate forms this workflow for unmarshalling (a) and deserialization (b):
```
Request Body    -(a)->    Map<String, Object>    -(b)->    Domain Object
```
and this workflow for serialization (c) and marshalling (d):
```
Domain Object    -(c)->    Map<String, Object>    -(d)->    Response Body
```

In fact, MapMate is so powerful at doing (de-)serialization that we just need to point it to the package where our domain objects
(`MultiplicationRequest`, `CalculationResponse` and `Number`)
reside and it will auto-detect their structure and be able to conveniently perform the desired mapping
(please refer to MapMate's documentation if you want to learn more about this feature).
Using Gson for marshalling, we end up with a very lean and readable configuration:
```java
final Gson gson = new Gson();
final MapMate mapMate = aMapMate("com.envimate.httpmate.documentation.xx_usecases.calculation")
        .usingJsonMarshaller(gson::toJson, gson::fromJson)
        .build();
final HttpMate httpMate = anHttpMate()
        .post("/multiply", MultiplicationUseCase.class)
        .configured(toUseMapMate(mapMate))
        .build();
```
