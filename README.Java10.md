How to run with Java 10
=======================

https://sdkman.io/
$ sdk use java 10.0.2-oracle
$ mvn -s settings.xml -B verify

Known Issues
============

https://issues.apache.org/jira/browse/GROOVY-8339 (does not seem to affect the build)
```
...
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.codehaus.groovy.reflection.CachedClass (file:/home/lestephane/.m2/repository/org/codehaus/groovy/groovy/2.4.14/groovy-2.4.14.jar) to method java.lang.Object.finalize()
...
```

