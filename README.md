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

[![build test deploy](https://github.com/bootique/bootique-swagger/actions/workflows/maven.yml/badge.svg)](https://github.com/bootique/bootique-swagger/actions/workflows/maven.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.bootique.swagger/bootique-swagger.svg?colorB=brightgreen)](https://search.maven.org/artifact/io.bootique.swagger/bootique-swagger/)

# bootique-swagger

Integrates [Swagger](http://swagger.io/) REST API documentation services with Bootique. Supports modern
[OpenAPI 3 specification](https://swagger.io/docs/specification/about/). Contains the following modules:

* `bootique-swagger`: a REST service to dynamically generate OpenAPI specifications as either
JSON or YAML. Combines metadata from annotated API resources within the application with
[static API descriptors](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Integration-and-Configuration#known-locations)
to produce application-specific API specs.

* `bootique-swagger-ui`: embeddable Swagger web UI to visualize and interact with API specifications. Supports both
OpenAPI 3 and legacy Swagger 2 specifications.
  
## Usage

### Prerequisites

Include ```bootique-bom```:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>2.0.M1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Publishing API Specifications

`bootique-swagger` can generate API specification by combining multiple YAML/JSON specifications as well as
endpoint metadata (class/methods signatures, JAX-RS annotations such as `@Path`, `@GET`, etc. and
[Swagger annotations](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations). To include one or
more specification resources, add the following dependency:
```xml
<dependency>
	<groupId>io.bootique.swagger</groupId>
	<artifactId>bootique-swagger</artifactId>
</dependency>
```
And then configure the layout and the sources of the specs:

```yaml
swagger:
  specs:
    # arbitrary name of a spec endpoint... Multiple specs at different URLs are supported
    default:

      # desired URL paths of JSON and YAML resources. Resolved relative to Jersey root URL
      pathJson: "model/openapi.json"
      pathYaml: "model/openapi.yaml"

      # where the spec sources are
      spec: "classpath:main-openapi.yml"
      overrideSpec: "classpath:extra-openapi.yml"
      resourcePackages:
        - "com.example.api"
      resourceClasses:
        - "com.example.Api"
```
You can use any combination of "spec", "overrideSpec", "resourcePackages" and "resourceClasses". "spec" is usually appropriate for
"design-first" approach, "resourcePackages" and "resourceClasses" - for the "code-first". "overrideSpec" can be used with both to add
extra information. The order of loading is:

1. "resourcePackages" / "resourceClasses": This provides the metadata collected from endpoint Java classes and annotations.
2. "spec": This is a YAML or JSON file. Combined with "1", overriding any common properties.
3. "overrideSpec": This is a YAML or JSON file. Combined with "1" and "2", overriding any common properties in both.

Now, when you run the app, you should be able to access the specs at the URLs similar to these:

* http://127.0.0.1:8080/model/openapi.json
* http://127.0.0.1:8080/model/openapi.yaml

### Web UI

Bootique integrates Swagger browser UI to be able to view and interact with the API specs:

```xml
<dependency>
	<groupId>io.bootique.swagger</groupId>
	<artifactId>bootique-swagger-ui</artifactId>
</dependency>
```

To view the spec from the same app (e.g. the one added via `bootique-swagger-openapi` as described above), add the
relative path of the model resource to the app configuration:
```yml
swaggerui:
  default:
    specPath: model/openapi.json
```
When you start the application, the console will be available at `/<appcontext>/swagger-ui`. E.g.
http://127.0.0.1:8080/swagger-ui/ .

If you are running behind a proxy, make sure you pass the correct `Host` header with the `host[:port]` of the proxy,
or the browser will not be able to discover your specification endpoint and/or won't be able to invoke it properly.
E.g. for `nginx` proxy you might use the following config:

```
proxy_set_header  Host $http_host;
```

To view a spec from another app, configure specification public URL like this:
```yml
swaggerui:
  default:
    specUrl: https://example.org/model/openapi.json
```

