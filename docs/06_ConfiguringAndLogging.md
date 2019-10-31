# Configuring HttpMate and Logging

HttpMate is a flexible web framework. In order to
customize it to your needs, you need a way to configure it.
The HttpMate builder offers a `.configured()` method for this.
It takes a `Configurator` object as an argument. All HttpMate
integrations described throughout this guide will offer convenient
static methods that create these configurators for you
(we will call them *configurator methods* in the following chapters).
As an example, if you want to configure how HttpMate will log,
you can use the configurator methods provided in the `LoggerConfigurators` class:

- `toLogUsing()` - logs using the passed implementation of the `Logger` interface

- `toLogToStdout()` - logs everything to `STDOUT`

- `toLogToStderr()` - logs everything to `STDERR`

- `toLogToStdoutAndStderr()` - logs everything to both `STDOUT` and `STDERR`

- `toDropAllLogMessages()` - does not log at all

The default setting is to log to both `STDOUT` and `STDERR` - if you want to
change that to log only to `STDOUT`, the configuration would look like this:

```java
final HttpMate build = anHttpMate()
                [...]
                .configured(toLogToStdout())
                .build();
```