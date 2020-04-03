package io.bootique.swagger.ui.selenium;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@OpenAPIDefinition
@Path("/")
public class TestApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Returns hello message")
    public String get() {
        return "TEST";
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Returns hello message")
    public String post(String request) {
        return request;
    }

}