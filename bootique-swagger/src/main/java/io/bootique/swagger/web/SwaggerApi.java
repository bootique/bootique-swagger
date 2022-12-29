package io.bootique.swagger.web;

import io.bootique.swagger.OpenApiModel;
import io.bootique.swagger.SwaggerOpenapiApi;
import io.bootique.swagger.service.SwaggerService;
import io.swagger.v3.oas.annotations.Operation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Objects;

@Path("_this_is_a_placeholder_that_will_be_replaced_dynamically_")
public class SwaggerApi {

  static final String MEDIA_TYPE_JSON = MediaType.APPLICATION_JSON;
  static final String MEDIA_TYPE_YAML= "application/yaml";

  private final SwaggerService service;

  public SwaggerApi(SwaggerService service) {
    this.service = service;
  }

  @GET
  @Produces({MEDIA_TYPE_JSON, MEDIA_TYPE_YAML})
  @Operation(hidden = true)
  public Response getOpenApi(@Context UriInfo uriInfo) {
    var path = uriInfo.getPath();
    return service.getOpenApiModel(path)
        .map(model -> toResponse(path, model))
        .orElseGet(() -> notFound(path));
  }
  private Response toResponse(String path, OpenApiModel model) {
    var mediaType = getMediaType(path, model);
    return Response.status(Response.Status.OK)
        .entity(model.render(path))
        .type(mediaType)
        .build();
  }
  private Response notFound(String path) {
    return Response
        .status(Response.Status.NOT_FOUND)
        .type(MediaType.TEXT_PLAIN_TYPE)
        .entity("No model at " + path).build();
  }

  private String getMediaType(String path, OpenApiModel model) {
    return Objects.equals(model.getPathJson(), path) ? SwaggerOpenapiApi.MEDIA_TYPE_JSON : SwaggerOpenapiApi.MEDIA_TYPE_YAML;
  }
}
