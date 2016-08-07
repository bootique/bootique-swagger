[![Build Status](https://travis-ci.org/bootique/bootique-swagger.svg)](https://travis-ci.org/bootique/bootique-swagger)

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

