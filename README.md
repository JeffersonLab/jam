# beam-auth
A [Java EE 8](https://en.wikipedia.org/wiki/Jakarta_EE) web application for beam authorization at Jefferson Lab built with the [Smoothness](https://github.com/JeffersonLab/smoothness) web template.

---
 - [Build](https://github.com/JeffersonLab/beam-auth#build)
 - [Configure](https://github.com/JeffersonLab/beam-auth#configure)
 - [Install](https://github.com/JeffersonLab/beam-auth#install)
---

## Build
This [Java 11](https://adoptopenjdk.net/) project uses the [Gradle 5](https://gradle.org/) build tool to automatically download dependencies and build the project from source:

```
git clone https://github.com/JeffersonLab/beam-auth
cd beam-auth
gradlew build
```
**Note**: If you do not already have Gradle installed, it will be installed automatically by the wrapper script included in the source

**Note**: Jefferson Lab has an intercepting [proxy](https://gist.github.com/slominskir/92c25a033db93a90184a5994e71d0b78)

## Configure

### Environment Variables
Uses the [Smoothness Environment Variables](https://github.com/JeffersonLab/smoothness#environment-variables)

### Database
The Beam Auth application requires an Oracle 18 database with the following [schema](https://github.com/JeffersonLab/beam-auth/tree/main/schema) installed.   The application server hosting the Beam Auth app must also be configured with a JNDI datasource.

## Install
   1. Download [Wildfly 16](https://www.wildfly.org/downloads/)
   1. Download [beam-auth.war](https://github.com/JeffersonLab/beam-auth/releases) and drop it into the Wildfly webaps directory
   1. Start Wildfly and navigate your web browser to localhost:8080/beam-auth

**Note:** beam-auth presumably works with any Java EE 8 compatible server such as [GlassFish](https://javaee.github.io/glassfish/) or [TomEE](https://tomee.apache.org/).

**Note:** The dependency jars are included in the _war_ file that is generated by the build. 

