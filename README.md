<!--
     Licensed to the ObjectStyle LLC under one
   or more contributor license agreements.  See the NOTICE file
   distributed with this work for additional information
   regarding copyright ownership.  The ObjectStyle LLC licenses
   this file to you under the Apache License, Version 2.0 (the
   “License”); you may not use this file except in compliance
   with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License.
  -->

[![Build Status](https://travis-ci.org/bootique/bootique-swagger.svg)](https://travis-ci.org/bootique/bootique-swagger)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bootique.swagger/bootique-swagger/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bootique.swagger/bootique-swagger/)

# bootique-swagger

Integration of [Swagger API ](http://swagger.io/) documentation web service and libraries for Bootique apps. 

## Usage

Include ```bootique-swagger```:
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

...

<dependency>
	<groupId>io.bootique.swagger</groupId>
	<artifactId>bootique-swagger</artifactId>
</dependency>
```

Now you can use Swagger annotations on your JAX-RS resources. Swagger endpoint will be available at ```<your_rest_resources_root>/swagger.json``` or ```<your_rest_resources_root>/swagger.yaml```.

