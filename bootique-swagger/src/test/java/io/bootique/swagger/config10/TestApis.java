package io.bootique.swagger.config10;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

@Path("/t")
public class TestApis {

    @GET
    @Path("hi")
    public String sTest() {
        return "Hi, /t!";
    }

    @GET
    @Path("1")
    public TestApiModels.TestO1 sPath1() {
        return null;
    }

    @PUT
    @Path("1")
    public void sPath1(TestApiModels.TestO1 data) {
    }

    @GET
    @Path("2")
    public TestApiModels.TestO2 sPath2() {
        return null;
    }

    @GET
    @Path("3")
    public TestApiModels.TestO3 sPath3() {
        return null;
    }

    @GET
    @Path("4")
    public TestApiModels.TestO4 sPath4() {
        return null;
    }

}
