# Use-case driven flavour
The use-case driven flavour exists to catch architectural requirements of larger projects
where managing requests and mapping them to actual domain logic becomes unfeasable. 
Instead of calling the domain logic from handlers, you register it to HttpMate
on a much higher level in the form of so-called use cases. These are classes with one single public method
that will reflect one single feature of your application. For example:
```java
public final class MakeReservationUseCase {
    private final RestaurantTimetable restaurantTimetable;
    
    ...
    
    public ReservationConfirmation makeReservation(final Reservation reservation) {
        return restaurantTimetable.reserve(reservation);
    }
}
```
This is an infrastructure-agnostic way to access the domain logic.
If you decide to serve this using http, just spin a up an HttpMate with the use case flavour:
```java
useCaseDrivenBuilder()
        .post("/makeReservation", MakeReservationUseCase.class)
        ...
        .build();
```
HttpMate will now direct POST requests on `/makeReservation` to the `makeReservation` method.

## Object mapping
Until this point, it is unclear how HttpMate manages to get the `Reservation` parameter needed to call
`makeReservation`.  In order to achieve this, you need to give HttpMate a way to map
the body of incoming requests to method parameters. The recommended way to do this is using [MapMate](https://github.com/envimate/mapmate).
Once you have configured a MapMate instance to deserialize objects of type `Reservation`, the complete HttpMate configuration will look like this:
```java
useCaseDrivenBuilder()
        .post("/makeReservation", MakeReservationUseCase.class)
        .mappingRequestsAndResponsesUsing(mapMateIntegration(MAP_MATE).build())
        .build();
```
On every request, HttpMate will now interpret the request's body according to its
`Content-Type` (e.g. Json, XML, YAML) and use the provided MapMate instance to deserialize
the necessary `Reservation` object from it. After the use case has been invoked, it will
also use the provided MapMate instance to serialize the returned `ReservationConfirmation` to a
response body of the respective format (e.g. Json, XML, YAML).

## Low-level handlers in the use-case driven builder
Any feature that has been described for the low-level flavour can also be used in the use-case driven builder. In order to
register a low-level handler in a use-case driven HttpMate configuration, you need to add a `configured` statement:
```java
useCaseDrivenBuilder()
        .post("/makeReservation", MakeReservationUseCase.class)
        .mappingRequestsAndResponsesUsing(mapMateIntegration(MAP_MATE).build())
        .configured(toHandleGetRequestsTo("/hello", (request, response) -> response.setBody("hi!")))
        .build();
```

## Using authentication data in parameter mapping

