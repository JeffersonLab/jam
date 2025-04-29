# jam [![CI](https://github.com/JeffersonLab/jam/actions/workflows/ci.yaml/badge.svg)](https://github.com/JeffersonLab/jam/actions/workflows/ci.yaml) [![Docker](https://img.shields.io/docker/v/jeffersonlab/jam?sort=semver&label=DockerHub)](https://hub.docker.com/r/jeffersonlab/jam)
A [Java EE 8](https://en.wikipedia.org/wiki/Jakarta_EE) web application for both beam and RF operations authorization at Jefferson Lab built with the [Smoothness](https://github.com/JeffersonLab/smoothness) web template.

![Screenshot](https://github.com/JeffersonLab/jam/raw/main/Screenshot.png?raw=true "Screenshot")

---
 - [Overview](https://github.com/JeffersonLab/jam#overview)
 - [Quick Start with Compose](https://github.com/JeffersonLab/jam#quick-start-with-compose) 
 - [Install](https://github.com/JeffersonLab/jam#install)
 - [Configure](https://github.com/JeffersonLab/jam#configure)
 - [Build](https://github.com/JeffersonLab/jam#build)
 - [Develop](https://github.com/JeffersonLab/jam#develop) 
 - [Release](https://github.com/JeffersonLab/jam#release)
 - [Deploy](https://github.com/JeffersonLab/jam#deploy)
 - [See Also](https://github.com/JeffersonLab/jam#see-also)  
---

## Overview
The authorization application allows the Director of Operations (or a delegate) to clearly communicate and document authorization for various facilities at JLab to either generate beam or engage in RF operations. For beam generation the maximum current and beam mode ("permissions") that are authorized are provided for a given beam destination.  This information is stored in a database and presented via the web for easy access.   There are three beam generating facilities, each with their own set of beam destinations and beam modes: CEBAF, LERF, UITF.  There are seven facilities which generate RF: CEBAF Injector, CEBAF North Linac, CEBAF South Linac, LERF, CMTF, and VTA.  In addition to director authorization, the app also tracks Credited Controls and their verification.  Each beam destination is assigned a set of controls and each control is assigned to a particular responsible verification team.  A beam desintation is ready for beam only if all the controls assigned are verified by their responsible verification team.  Both team verifications and director permissions have expirations.   Emails and Jefferson Lab logbook entires are created to aid communication of new director permissions, responsible team verifications (upgrades and downgrades), and verification and permissions expirations.

### Roles
 - **Authorizer / Operations Director** - Responsible for authorizing beam
 - **Operability / Admin** - Responsible for process administration and continuous improvement
 - **Operator** - Must honor permissions set by the Director when operating facilities
 - **Verification Team** - Responsible for verifying Credited Control readiness and assigning expiration dates for when new checks are required

### JLab Internal Docs
 - [CEBAF-LERF ASE](https://jlabdoc.jlab.org/docushare/dsweb/Get/Document-187898)
 - [UITF ASE](https://jlabdoc.jlab.org/docushare/dsweb/Get/Document-203051)
 - [SAD](https://jlabdoc.jlab.org/docushare/dsweb/Get/Document-21395)

## Quick Start with Compose
1. Grab project
```
git clone https://github.com/JeffersonLab/jam
cd jam
```
2. Launch [Compose](https://github.com/docker/compose)
```
docker compose up
```
3. Navigate to page
```
http://localhost:8080/jam
```

**Note**: Login with demo username "tbrown" and password "password".

**See**: [Docker Compose Strategy](https://gist.github.com/slominskir/a7da801e8259f5974c978f9c3091d52c)

## Install
This application requires a Java 11+ JVM and standard library to run, plus a Java EE 8+ application server (developed with Wildfly).

1. Install service [dependencies](https://github.com/JeffersonLab/jam/blob/main/deps.yaml)
2. Download [Wildfly 26.1.3](https://www.wildfly.org/downloads/)
3. [Configure](https://github.com/JeffersonLab/jam#configure) Wildfly and start it
4. Download [jam.war](https://github.com/JeffersonLab/jam/releases) and deploy it to Wildfly
5. Navigate your web browser to [localhost:8080/jam](http://localhost:8080/jam)

## Configure

### Configtime
Wildfly must be pre-configured before the first deployment of the app.  The [wildfly bash scripts](https://github.com/JeffersonLab/wildfly#configure) can be used to accomplish this.  See the [Dockerfile](https://github.com/JeffersonLab/jam/blob/main/Dockerfile) for an example.

### Runtime
Uses the [Smoothness Environment Variables](https://github.com/JeffersonLab/smoothness#global-runtime) plus the following application specific Settings:

| Name                              | Description                                                                                                                                 |
|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| COMPONENT_DETAIL_URL          | URL for Component detail linking. Example: https://ace.jlab.org/srm/reports/component?name=                                                 |
| COMPONENT_QUERY_URL           | URL for Component search. Proxied. Example: https://ace.jlab.org/srm/data/components                                                        |
| USER_QUERY_URL                | URL for User search.  Proxied. Example: https://ace.jlab.org/srm/ajax/search-user                                                           |

### Database
This application requires an Oracle 18+ database with the following [schema](https://github.com/JeffersonLab/jam/tree/main/container/oracle/initdb.d) installed.   The application server hosting this app must also be configured with a JNDI datasource.

## Build
This project is built with [Java 17](https://adoptium.net/) (compiled to Java 11 bytecode), and uses the [Gradle 7](https://gradle.org/) build tool to automatically download dependencies and build the project from source:

```
git clone https://github.com/JeffersonLab/jam
cd jam
gradlew build
```
**Note**: If you do not already have Gradle installed, it will be installed automatically by the wrapper script included in the source

**Note for JLab On-Site Users**: Jefferson Lab has an intercepting [proxy](https://gist.github.com/slominskir/92c25a033db93a90184a5994e71d0b78)

**See**: [Docker Development Quick Reference](https://gist.github.com/slominskir/a7da801e8259f5974c978f9c3091d52c#development-quick-reference)

## Develop
In order to iterate rapidly when making changes it's often useful to run the app directly on the local workstation, perhaps leveraging an IDE.  In this scenario run the service dependencies with:
```
docker compose -f deps.yaml up
```
**Note**: The local install of Wildfly should be [configured](https://github.com/JeffersonLab/jam#configure) to proxy connections to services via localhost and therefore the environment variables should contain:
```
KEYCLOAK_BACKEND_SERVER_URL=http://localhost:8081
FRONTEND_SERVER_URL=https://localhost:8443
```
Further, the local DataSource must also leverage localhost port forwarding so the `standalone.xml` connection-url field should be: `jdbc:oracle:thin:@//localhost:1521/xepdb1`.  

The [server](https://github.com/JeffersonLab/wildfly/blob/main/scripts/server-setup.sh) and [app](https://github.com/JeffersonLab/wildfly/blob/main/scripts/app-setup.sh) setup scripts can be used to setup a local instance of Wildfly. 

## Release
1. Bump the version number in the VERSION file and commit and push to GitHub (using [Semantic Versioning](https://semver.org/)).
2. The [CD](https://github.com/JeffersonLab/jam/blob/main/.github/workflows/cd.yaml) GitHub Action should run automatically invoking:
    - The [Create release](https://github.com/JeffersonLab/java-workflows/blob/main/.github/workflows/gh-release.yaml) GitHub Action to tag the source and create release notes summarizing any pull requests.   Edit the release notes to add any missing details.  A war file artifact is attached to the release.
    - The [Publish docker image](https://github.com/JeffersonLab/container-workflows/blob/main/.github/workflows/docker-publish.yaml) GitHub Action to create a new demo Docker image.
    - The [Deploy to JLab](https://github.com/JeffersonLab/general-workflows/blob/main/.github/workflows/jlab-deploy-app.yaml) GitHub Action to deploy to the JLab test environment.

## Deploy
The deploy to JLab's acctest is handled automatically via the release workflow.

At JLab this app is found at [ace.jlab.org/jam](https://ace.jlab.org/jam) and internally at [acctest.acc.jlab.org/jam](https://acctest.acc.jlab.org/jam).  However, those servers are proxies for `wildfly5.acc.jlab.org` and `wildflytest5.acc.jlab.org` respectively.   A [deploy script](https://github.com/JeffersonLab/wildfly/blob/main/scripts/deploy.sh) is provided on each server to automate wget and deploy.  Example:

```
/root/setup/deploy.sh jam v1.2.3
```

**JLab Internal Docs**:  [InstallGuideWildflyRHEL9](https://accwiki.acc.jlab.org/do/view/SysAdmin/InstallGuideWildflyRHEL9)

## See Also
 - [JLab ACE management-app list](https://github.com/search?q=org%3Ajeffersonlab+topic%3Aace+topic%3Amanagement-app&type=repositories)
