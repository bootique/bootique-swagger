<!--
  Licensed to ObjectStyle LLC under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ObjectStyle LLC licenses
  this file to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  -->

[![Build Status](https://travis-ci.org/bootique/bootique-swagger.svg)](https://travis-ci.org/bootique/bootique-swagger)
[![Maven Central](https://img.shields.io/maven-central/v/io.bootique.swagger/bootique-swagger.svg?colorB=brightgreen)](https://search.maven.org/artifact/io.bootique.swagger/bootique-swagger/)

# bootique-swagger

Integrates [Swagger](http://swagger.io/) REST API documentation services into Bootique. Supports modern
[OpenAPI specification](https://swagger.io/docs/specification/about/), as well as legacy Swagger 2. Contains the
following modules:

* `bootique-swagger-openapi`: provides a REST service to dynamically generate OpenAPI specification resources as either
JSON or YAML. Combines the information from the app API resources annotated with JAX-RS and
[Swagger annotations](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations) with
[static API descriptors](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Integration-and-Configuration#known-locations)
to produce application-specific API models.

* `bootique-swagger`: a legacy service generating Swagger 2 API descriptors.

* `bootique-swagger-ui`: provides Swagger web UI that allows to visually inspect in-app and external API models (both
in OpenAPI and legacy Swagger 2 formats).

_TODO: the examples shows yet unreleased 2.0 API, only available as snapshots_

## Usage

### Prerequisites

Include ```bootique-bom```:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>2.0-SNAPSHOTS</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### "Code-First" - exposing annotated endpoints

Swagger can generate application model dynamically based purely on the endpoint class/method signatures and JAX-RS
annotations (such as `@Path`, `@GET`, etc.). You would usually enrich this model via a few mechanisms:

* [Swagger annotations](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations).
* [Static API descriptors](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Integration-and-Configuration#known-locations)

To expose the API model as both JSON and YAML, add the following dependency:
```xml
<dependency>
	<groupId>io.bootique.swagger</groupId>
	<artifactId>bootique-swagger-openapi</artifactId>
</dependency>
```
Now, when you run the app, you should be able to access the models at URLs similar to these:

* http://127.0.0.1:8080/swagger.json
* http://127.0.0.1:8080/swagger.yaml

### Web UI

Bootique integrates Swagger browser UI to be able to view and interact with the API models:

```xml
<dependency>
	<groupId>io.bootique.swagger</groupId>
	<artifactId>bootique-swagger-ui</artifactId>
</dependency>
```

To access tge model built into the same app (e.g. via `bootique-swagger-openapi` as described above), add the following
configuration to the app:

```yml
swaggerui:
  specPath: openapi.json
```
Alternatively you can access models via any public URL as follows:
```yml
swaggerui:
  specUrl: https://example.org/path/to/openapi.json
```

When you start the application, the console will be available as `/myapp/swagger-ui`. E.g. http://127.0.0.1:8080/swagger-ui/ .
