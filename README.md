[![Build Status](https://travis-ci.org/bootique/bootique-swagger.svg)](https://travis-ci.org/bootique/bootique-swagger)

# bootique-swagger

Integration of Swagger API documentation web service and libraries for Bootique apps. 

## Usage

Include ```bootique-swagger```:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>0.18</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>com.nhl.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>0.18</version>
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