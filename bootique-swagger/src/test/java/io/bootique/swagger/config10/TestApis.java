package io.bootique.swagger.config10;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

@Path("/t")
public class TestApis {

    @GET
    @Path("hi")
    @Tag(name = "hi")
    public String sTest() {
        return "Hi, /t!";
    }

    @GET
    @Path("1")
    @Tag(name = "one")
    public TestApiModels.TestO1 sPath1() {
        return null;
    }

    @PUT
    @Path("1")
    @Tag(name = "one")
    public void sPath1(TestApiModels.TestO1 data) {
    }

    @GET
    @Path("2")
    @Tag(name = "two")
    public TestApiModels.TestO2 sPath2() {
        return null;
    }

    @GET
    @Path("3")
    @Tag(name = "shared")
    public TestApiModels.TestO3 sPath3() {
        return null;
    }

    @GET
    @Path("4")
    @Tag(name = "shared")
    public TestApiModels.TestO4 sPath4() {
        return null;
    }

}
