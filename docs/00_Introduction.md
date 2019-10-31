# Introduction
HttpMate is a modern and lightweight http framework for the Java ecosystem. This guide will
explain everything you need to know to use it successfully in your projects.
It will start by walking you through all basic concepts of http
and how they can be used with HttpMate. It will cover topics like normal
request processing, downloads, uploads, security, websockets and a lot more.
If you are new to HttpMate, just start from the very beginning with
the quickstart.

Beyond "normal" http handling that works similar to other projects like Jetty,
HttpMate is structured to mitigate architectural problems
commonly found in serious projects.
One of the issues that is addressed in HttpMate is keeping an architectural boundary
between the code that handles http processing (infrastructure code) and
the code that handles your actual business logic (domain code).
In our experience, it is quite hard to achieve this goal using the common and
established application frameworks of the Java ecosystem.
HttpMate offers a solution to this problem by directly serving your domain
logic, without the need of an intermediate layer consisting of annotations and/or
"controllers". If you are an advanced reader and are primarily interested
in the architectural features of HttpMate, you may skip to the "UseCases"
section.