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
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bootique.swagger/bootique-swagger/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bootique.swagger/bootique-swagger/)

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

Include ```bootique-bom```:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>0.19</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

For "code-first" flow, annotate your REST resources with Swagger annotations and then include `bootique-swagger`:

<dependency>
	<groupId>io.bootique.swagger</groupId>
	<artifactId>bootique-swagger</artifactId>
</dependency>
```

This adds a few resources to your app:

* `<your_rest_resources_root>/swagger.json`. E.g. http://127.0.0.1:8080/swagger.json
*  `<your_rest_resources_root>/swagger.yaml`. E.g. http://127.0.0.1:8080/swagger.yaml


To include UI console inside the app:

```xml
<dependency>
	<groupId>io.bootique.swagger</groupId>
	<artifactId>bootique-swagger-ui</artifactId>
</dependency>
```

The UI will be accessible at `your_rest_resources_root/swagger-ui`. E.g. http://127.0.0.1:8080/swagger-ui/



