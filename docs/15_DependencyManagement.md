# Maven

HttpMate can provide seriously lightweight HTTP endpoints while at the same
time offering a rich set of features. Since not every user of HttpMate needs to
use all offered features, we have spread them across multiple maven dependencies in order to
keep the individual dependency footprint small. This document provides guidance to anyone
who needs to integrate HttpMate dependencies into a new or existing Maven project.

## I just want to try it
All HttpMate integrations have been bundled into a single maven module so that
initial users don't need to bother choosing the correct dependencies. If you
are new to HttpMate and just want to experience HttpMate for the first time,
just include this dependency:
```xml
<dependency>
    <groupId>com.envimate.httpmate.integrations</groupId>
    <artifactId>httpmate-all</artifactId>
    <version>${httpmate.version}</version>
</dependency>
```
This contains anything HttpMate has to offer, but be aware that it probably adds
way more than you need and should not be used in serious projects.
## Production-quality setup
Any setup beyond a simple 5-minute trial run should follow an approach that only adds
dependencies which are actually used.

### The core module
Every HttpMate configuration needs to include the core module:
```xml
<dependency>
    <groupId>com.envimate.httpmate</groupId>
    <artifactId>core</artifactId>
    <version>${httpmate.version}</version>
</dependency>
```
It contains the basic HttpMate builder and the [PureJavaEndpoint](UserGuide.md#Pure Java).

### Integration modules
Depending on which HttpMate features you intend to use, you need to load additional
dependencies. All integrations and their respective Maven coordinates can be found
in the `/integrations` project subdirectory.