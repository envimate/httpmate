# Event driven
The use-case driven flavour allows you to architecturally scale your application by giving you a way to configure http handling once and
then just add new use-cases without having to add new infrastructure configuration.
The event-driven flavour will keep these properties, but will additionally allow you to scale technically. It allows you
to distribute HttpMate over different nodes by dispatching http requests to a MessageBus. Example:

```java
anHttpMateConfiguredAs(EVENT_DRIVEN).attachedTo(messageBus)
                .triggeringTheEvent("MakeReservation").forRequestPath("/makeReservation").andRequestMethod(POST)
                .mappingResponsesUsing((event, metaData) -> metaData.set(BODY_STRING, event.toString()))
                .build();
```

This instance of HttpMate will forward any POST request on `makeReservation` to the provided message bus as an event of
type `MakeReservation`.

## Events
HttpMate uses the `MessageMate` project to deal with events. In `MessageMate`, events are represented as
an Object of type `Map<String, Object` i.e. a `String` map. HttpMate will generate one event per http request.

## From http requests to events
In order to issue events on each incoming http request, HttpMate needs to have a way to map these requests to
respective events.
By default, the body of the http request will be parsed into a map according to the body's `Content-Type`. For example,
given this body:

```json
{
  "name": "joe doe",
  "state": "NY"
}
```
and the content type `application/json`, HttpMate will issue an event that is equal to a `String` map with
these entries:
```
"name" = "joe doe"
"state" = "NY"
```

## Subscribing to events
Sometimes you want to react to incoming events. In order to do this,
  