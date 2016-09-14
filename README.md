# jcfl
Java class file loader

Introduction
============

JCFL is a system used to represent Java code in a way suitable for
analysis.

Loading Classes
===============

The Gradle main class is `com.jbalint.jora.proto.JarToRdf`. A jar must
be given on the command line. The result will be created in a
subdirectory called `output` under the directory from which Gradle is
invoked.

```
$ gradle run -Pjar=$JAVA_HOME/jre/lib/rt.jar
```

```
$ stardog data add db -g "http://jbalint/javap/jar#rt.jar" output/rt.jar.javap.ttl
```