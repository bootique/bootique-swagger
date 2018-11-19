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

This project integrates [Swagger API](http://swagger.io/) documentation services into Bootique.
It contains two modules:

* `bootique-swagger`: provides a REST service to dynamically generate JSON or YAML
file, containing Sagger model. Generation is done from JAX-RS endpoints, annotated
with Swagger documentation annotations. I.e. it appropriate for Swagger "code-first"
flow.

* `bootique-swagger-ui`: provides swagger web UI that allows to inspect
current app Swagger API model, as well as send test requests.

## Usage

### Prerequisites

Include ```bootique-bom```:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>0.25</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Next we need to provide a service that returns your app API spec (aka
`swagger.json`). This service can then be used for manual inspection of
the API structure, or more often accessed via the Swagger web UI, as described
further down in this document. There are a few ways to serve `swagger.json`,
depending on your workflow. Let's look at two of them - a static file
and dynamically generated REST resource.

### Static `swagger.json`

In this scenario, developer bundles `swagger.json` with her app and makes
it acessible via `bootique-jetty`. There are a few ways to obtain this JSON
file. Typically you'd write `swagger.yaml` by hand, and then convert it
to `swagger.json` with `swagger-codegen` [tool](https://github.com/swagger-api/swagger-codegen)
that has Maven plugin and CLI flavors. From here the steps are the same
as with any static file that you want to expose via HTTP in a Bootique app.
Here is one possible setup:

* Designate a "docroot" directory as a subdirectory of the project resources.
E.g. `src/main/resources/doctroot`
* Configure `bootique-jetty` to include the "default" servlet rooted in this
directory:

```java
JettyModule.extend(binder).useDefaultServlet();
BQCoreModule.extend(binder).setProperty("bq.jetty.staticResourceBase", "classpath:docroot");
```

* Put `swagger.json` under docroot (i.e. at  `src/main/resources/doctroot/swagger.json`).

Start the app and verify that the JOSN is accessible. E.g. at http://127.0.0.1:8080/swagger.json

### `swagger.json` Auto-Generated from Java Annotations

In a typical "code-first" flow, you might manually annotate your Java
REST resources with Swagger annotations instead of creating a static
`swagger.json`. In this case include `bootique-swagger` dependency to
dynamically generate the API model in runtime:

```xml
<dependency>
	<groupId>io.bootique.swagger</groupId>
	<artifactId>bootique-swagger</artifactId>
</dependency>
```

This results in two dynamic resources being added to your app, with URLs
relative to your Jersey root URL:

* `<your_rest_resources_root>/swagger.json`. E.g. http://127.0.0.1:8080/swagger.json
* `<your_rest_resources_root>/swagger.yaml`. E.g. http://127.0.0.1:8080/swagger.yaml


### Web UI (Since 1.0.RC1)

To include UI console inside the app:

```xml
<dependency>
	<groupId>io.bootique.swagger</groupId>
	<artifactId>bootique-swagger-ui</artifactId>
</dependency>
```

The UI will be accessible at `your_rest_resources_root/swagger`. E.g. http://127.0.0.1:8080/swagger/ .
The static resources of swagger ui will be accessible at `your_rest_resources_root/static`. E.g. http://127.0.0.1:8080/static/swagger-ui.css .

